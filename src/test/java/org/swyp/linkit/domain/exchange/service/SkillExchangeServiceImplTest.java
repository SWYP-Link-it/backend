package org.swyp.linkit.domain.exchange.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.swyp.linkit.domain.credit.entity.Credit;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.entity.SupplyType;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.domain.exchange.dto.SkillExchangeDto;
import org.swyp.linkit.domain.exchange.dto.request.SkillExchangeRequestDto;
import org.swyp.linkit.domain.exchange.dto.response.*;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.SkillExchangeRepository;
import org.swyp.linkit.domain.exchange.repository.projection.SkillExchangeDetailQuery;
import org.swyp.linkit.domain.user.dto.AvailableScheduleDto;
import org.swyp.linkit.domain.user.entity.*;
import org.swyp.linkit.domain.user.service.AvailableScheduleService;
import org.swyp.linkit.domain.user.service.UserService;
import org.swyp.linkit.domain.user.service.UserSkillService;
import org.swyp.linkit.global.error.exception.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SkillExchangeService 단위 테스트")
class SkillExchangeServiceImplTest {

    @Mock
    AvailableScheduleService availableScheduleService;

    @Mock
    SkillExchangeRepository exchangeRepository;

    @Mock
    UserService userService;

    @Mock
    UserSkillService userSkillService;

    @Mock
    CreditService creditService;

    @InjectMocks
    SkillExchangeServiceImpl exchangeService;

    private static final int CREDIT_EXCHANGE_RATE_MINUTES = 30;
    private Long userId = 1L;
    private Long userSkillId = 1L;
    private Long exchangeId = 1L;
    private Long profileId = 1L;
    private Long creditId = 1L;
    private Long historyId = 1L;

    @Nested
    @DisplayName("멘토의 거래 가능 날짜 조회 (getAvailableDates)")
    class GetAvailableDates {
        private final User mentor = createUser();
        private final String month = "2026-02";

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {
            @Test
            @DisplayName("멘토의 거래 가능 날짜를 반환")
            public void success() {
                // given
                // mentor 조회 Mock 처리
                when(userService.getUserById(mentor.getId())).thenReturn(mentor);

                // mentor 의 3개월치 데이터 Mock 처리
                AvailableScheduleDto schedule1 = new AvailableScheduleDto(
                        LocalDate.of(2026, 2, 1),
                        "SUN", LocalTime.of(10, 0),
                        LocalTime.of(11, 0));
                AvailableScheduleDto schedule2 = new AvailableScheduleDto(
                        LocalDate.of(2026, 2, 1),
                        "SUN", LocalTime.of(11, 0),
                        LocalTime.of(12, 0));
                AvailableScheduleDto schedule3 = new AvailableScheduleDto(
                        LocalDate.of(2026, 2, 6),
                        "WED", LocalTime.of(10, 0),
                        LocalTime.of(12, 0));

                when(availableScheduleService.getExpandedSchedules(mentor.getId()))
                        .thenReturn(List.of(schedule1, schedule2, schedule3));
                // when
                AvailableDatesResponseDto response = exchangeService.getAvailableDates(mentor.getId(), month);

                // then
                // 중복 제거로 인해 2
                assertThat(response.getAvailableDates()).hasSize(2);
                assertThat(response.getAvailableDates().stream().allMatch(date -> date.startsWith(month))).isTrue();

                verify(userService).getUserById(mentor.getId());
                verify(availableScheduleService).getExpandedSchedules(mentor.getId());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {

            @Test
            @DisplayName("존재하지 않는 mentor로 인한 MentorNotFoundException 발생")
            public void fail_MentorNotFoundException() {
                // given
                doThrow(new UserNotFoundException())
                        .when(userService).getUserById(mentor.getId());

                // when && then
                assertThatThrownBy(() -> exchangeService.getAvailableDates(mentor.getId(), month))
                        .isInstanceOf(MentorNotFoundException.class);
            }

            @Test
            @DisplayName("해당 월에 가능한 스케줄이 존재하지 않아 ScheduleNotFoundException 발생")
            public void fail_ScheduleNotFoundException() {
                // given
                // mentor 조회 Mock 처리
                when(userService.getUserById(mentor.getId())).thenReturn(mentor);
                // 3개월치 데이터 빈 데이터 처리
                when(availableScheduleService.getExpandedSchedules(mentor.getId()))
                        .thenReturn(List.of());

                // when && then
                assertThatThrownBy(() -> exchangeService.getAvailableDates(mentor.getId(), month))
                        .isInstanceOf(ScheduleNotFoundException.class);

            }
        }
    }

    @Nested
    @DisplayName("멘토의 날짜 별 거래 가능 시간 조회 (getAvailableSlots)")
    class GetAvailableSlots {
        private final User mentorUser = createUser();
        private final User menteeUser = createUser();
        private final UserSkill receiverSkill1 = createUserSkill(30);
        private final UserSkill receiverSkill2 = createUserSkill(60);
        private final UserProfile mentorProfile = createUserProfile(mentorUser, List.of(receiverSkill1, receiverSkill2));
        private final LocalDate date = LocalDate.of(2026, 2, 4);

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {
            @Test
            @DisplayName("멘토의 거래 가능 날짜를 반환")
            public void success() {
                // given
                // receiverSkill 조회 Mock 처리
                when(userSkillService.getUserSkillWithProfileAndUser(receiverSkill1.getId())).thenReturn(receiverSkill1);

                // mentor 의 3개월치 데이터 Mock 처리
                AvailableScheduleDto schedule1 = new AvailableScheduleDto(
                        LocalDate.of(2026, 2, 4),
                        "SUN", LocalTime.of(10, 0),
                        LocalTime.of(12, 0));
                AvailableScheduleDto schedule2 = new AvailableScheduleDto(
                        LocalDate.of(2026, 2, 4),
                        "SUN", LocalTime.of(14, 0),
                        LocalTime.of(16, 0));
                AvailableScheduleDto schedule3 = new AvailableScheduleDto(
                        LocalDate.of(2026, 2, 4),
                        "WED", LocalTime.of(16, 30),
                        LocalTime.of(17, 0));
                AvailableScheduleDto schedule4 = new AvailableScheduleDto(
                        LocalDate.of(2026, 2, 4),
                        "WED", LocalTime.of(17, 30),
                        LocalTime.of(18, 0));
                when(availableScheduleService.getExpandedSchedules(mentorUser.getId()))
                        .thenReturn(List.of(schedule1, schedule2, schedule3, schedule4));

                // 해당 날짜에 예약 조회 Mock 처리
                SkillExchange exchange1 = createExchange(mentorUser, mentorUser, receiverSkill1,
                        LocalTime.of(10, 0), LocalTime.of(11, 0));
                SkillExchange exchange2 = createExchange(menteeUser, mentorUser, receiverSkill2,
                        LocalTime.of(14, 0), LocalTime.of(16, 0));
                when(exchangeRepository.findAllByReceiverIdAndDate(mentorUser.getId(), date, ExchangeStatus.CANCELED))
                        .thenReturn(List.of(exchange1, exchange2));

                // when
                AvailableSlotsResponseDto result = exchangeService.getAvailableSlots(mentorUser.getId(), receiverSkill1.getId(), date);

                // then
                List<SlotDto> filteredDto = result.getSlots().stream()
                        .filter(SlotDto::isAvailable).toList();
                assertThat(result.getSlots().size()).isEqualTo(10);
                assertThat(filteredDto.size()).isEqualTo(4);
                assertThat(filteredDto.get(0).getTime()).isEqualTo(LocalTime.parse("11:00"));
                assertThat(filteredDto.get(1).getTime()).isEqualTo(LocalTime.parse("11:30"));
                assertThat(filteredDto.get(2).getTime()).isEqualTo(LocalTime.parse("16:30"));
                assertThat(filteredDto.get(3).getTime()).isEqualTo(LocalTime.parse("17:30"));

                verify(userSkillService).getUserSkillWithProfileAndUser(receiverSkill1.getId());
                verify(exchangeRepository).findAllByReceiverIdAndDate(mentorUser.getId(), date, ExchangeStatus.CANCELED);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {

            @Test
            @DisplayName("존재하지 않는 userSkill로 인한 UserSkillNotFoundException 발생")
            public void fail_UserSkillNotFoundException() {
                // given
                doThrow(new UserSkillNotFoundException())
                        .when(userSkillService).getUserSkillWithProfileAndUser(receiverSkill1.getId());

                // when && then
                assertThatThrownBy(() -> exchangeService.getAvailableSlots(mentorUser.getId(), receiverSkill1.getId(), date))
                        .isInstanceOf(UserSkillNotFoundException.class);
            }

            @Test
            @DisplayName("mentor의 정보와 userSkill이 일치하지 않아 SkillMentorMissMatchException 발생")
            public void fail_SkillMentorMissMatchException() {
                // given
                // 진짜 멘토와 가짜 멘토 생성
                User realMentor = createUser();
                User skillOwner = createUser();

                // 스킬 생성 및 연관관계 주입
                UserSkill skill = createUserSkill(30);
                UserProfile profile = createUserProfile(skillOwner, List.of(skill));

                // 3. 스킬 조회 시 가짜 mentor 스킬 리턴
                when(userSkillService.getUserSkillWithProfileAndUser(skill.getId())).thenReturn(skill);

                // when && then
                assertThatThrownBy(() -> exchangeService.getAvailableSlots(realMentor.getId(), skill.getId(), date))
                        .isInstanceOf(SkillMentorMissMatchException.class);
            }
        }
    }

    @Nested
    @DisplayName("스킬 거래 요청 (requestSkillExchange)")
    class RequestSkillExchange {
        private User mentee = createUser();
        private User mentor = createUser();
        private UserSkill mentorSkill = createUserSkill(60);
        private UserProfile mentorProfile = createUserProfile(mentor, List.of(mentorSkill));
        private Credit menteeCredit = createCredit(mentee, 2);
        private LocalDate date = LocalDate.of(2026, 1, 25);
        private LocalTime startTime = LocalTime.of(20, 0);

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {
            @Test
            @DisplayName("스킬 거래 요청 처리")
            public void success() {
                // given
                // 멘티 조회 Mock 처리
                when(userService.getUserById(mentee.getId())).thenReturn(mentee);

                // 멘토 스킬 조회 Mock 처리
                when(userSkillService.getUserSkillWithProfileAndUserAndLock(mentorSkill.getId())).thenReturn(mentorSkill);

                // 멘토의 가능한 시간 조회 Mock 처리 -> date 날에 [10:00 ~ 12:00], [13:00 ~ 13:30], [20:00 ~ 22:00]
                AvailableScheduleDto schedule1 = new AvailableScheduleDto(
                        date, "SUN", LocalTime.of(10, 0), LocalTime.of(11, 0));
                AvailableScheduleDto schedule2 = new AvailableScheduleDto(
                        date, "SUN", LocalTime.of(13, 0), LocalTime.of(13, 30));
                AvailableScheduleDto schedule3 = new AvailableScheduleDto(
                        date, "SUN", LocalTime.of(20, 0), LocalTime.of(22, 0));
                when(availableScheduleService.getExpandedSchedules(mentor.getId())).thenReturn(List.of(schedule1, schedule2, schedule3));

                // 예약된 현황 조회 Mock 처리 -> date 날에 [10:00 ~ 10:30], [11:00 ~ 12:00]
                SkillExchange exchange1 = createExchange(mentee, mentor, mentorSkill, LocalTime.of(10, 0), LocalTime.of(10, 30));
                SkillExchange exchange2 = createExchange(mentee, mentor, mentorSkill, LocalTime.of(11, 0), LocalTime.of(12, 0));
                when(exchangeRepository.findAllByReceiverIdAndDate(mentor.getId(), date, ExchangeStatus.CANCELED))
                        .thenReturn(List.of(exchange1, exchange2));

                // 스킬 교환 저장 Mock 처리
                SkillExchange exchange = createExchange(mentee, mentor, mentorSkill, startTime, startTime.plusMinutes(mentorSkill.getExchangeDuration()));
                when(exchangeRepository.save(any(SkillExchange.class))).thenReturn(exchange);

                SkillExchangeRequestDto requestDto = new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(), "", date, startTime);
                SkillExchangeDto skillExchangeDto = SkillExchangeDto.from(requestDto);

                // when
                SkillExchangeResponseDto sut = exchangeService.requestSkillExchange(mentee.getId(), skillExchangeDto);

                // then
                assertThat(sut.getSkillExchangeId()).isEqualTo(exchange.getId());
                assertThat(sut.getExchangeStatus()).isEqualTo(ExchangeStatus.PENDING.getDescription());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {

            @Test
            @DisplayName("존재하지 않는 멘티로 인한 UserNotFoundException")
            public void fail_UserNotFoundException() {
                // given
                doThrow(new UserNotFoundException()).when(userService).getUserById(mentee.getId());

                SkillExchangeRequestDto requestDto = new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(), "", date, startTime);
                SkillExchangeDto skillExchangeDto = SkillExchangeDto.from(requestDto);

                // when && then
                assertThatThrownBy(() -> exchangeService.requestSkillExchange(mentee.getId(), skillExchangeDto))
                        .isInstanceOf(UserNotFoundException.class);
            }

            @Test
            @DisplayName("존재하지 않는 멘토의 스킬로 인한 UserSkillNotFoundException")
            public void fail_UserSkillNotFoundException() {
                // given
                // 멘티 조회 Mock 처리
                when(userService.getUserById(mentee.getId())).thenReturn(mentee);

                // 멘토 스킬 조회 Mock 처리
                doThrow(UserSkillNotFoundException.class).when(userSkillService)
                        .getUserSkillWithProfileAndUserAndLock(mentorSkill.getId());

                SkillExchangeRequestDto requestDto = new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(), "", date, startTime);
                SkillExchangeDto skillExchangeDto = SkillExchangeDto.from(requestDto);

                // when && then
                assertThatThrownBy(() -> exchangeService.requestSkillExchange(mentee.getId(), skillExchangeDto))
                        .isInstanceOf(UserSkillNotFoundException.class);
            }

            @Test
            @DisplayName("멘토의 스킬과 멘토 정보가 불일치로 인한 SkillMentorMissMatchException")
            public void fail_SkillMentorMissMatchException() {
                // given
                // 멘티 조회 Mock 처리
                when(userService.getUserById(mentee.getId())).thenReturn(mentee);

                // 멘토 스킬 조회 다른 유저의 skill로 Mock 처리
                User otherUser = createUser();
                UserSkill otherUserSkill = createUserSkill(60);
                createUserProfile(otherUser, List.of(otherUserSkill));
                when(userSkillService.getUserSkillWithProfileAndUserAndLock(mentorSkill.getId())).thenReturn(otherUserSkill);

                SkillExchangeRequestDto requestDto = new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(), "", date, startTime);
                SkillExchangeDto skillExchangeDto = SkillExchangeDto.from(requestDto);

                // when && then
                assertThatThrownBy(() -> exchangeService.requestSkillExchange(mentee.getId(), skillExchangeDto))
                        .isInstanceOf(SkillMentorMissMatchException.class);
            }

            @Test
            @DisplayName("미공개 스킬로 인한 SkillNotAvailableException")
            public void fail_SkillNotAvailableException() {
                // given
                // 멘티 조회 Mock 처리
                when(userService.getUserById(mentee.getId())).thenReturn(mentee);

                // 멘토 스킬 조회 미공개 skill로 Mock 처리
                UserSkill unVisibleUserSkill = createUnVisibleUserSkill(60);
                createUserProfile(mentor, List.of(unVisibleUserSkill));
                when(userSkillService.getUserSkillWithProfileAndUserAndLock(mentorSkill.getId())).thenReturn(unVisibleUserSkill);

                SkillExchangeRequestDto requestDto = new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(), "", date, startTime);
                SkillExchangeDto skillExchangeDto = SkillExchangeDto.from(requestDto);

                // when && then
                assertThatThrownBy(() -> exchangeService.requestSkillExchange(mentee.getId(), skillExchangeDto))
                        .isInstanceOf(SkillNotAvailableException.class);
            }

            @Test
            @DisplayName("본인 스킬 거래 요청으로 인한 SelfExchangeNotAllowedException")
            public void fail_SelfExchangeNotAllowedException() {
                // given
                // 멘티 조회 Mock 처리
                when(userService.getUserById(mentor.getId())).thenReturn(mentor);

                // 멘토 스킬 조회 Mock 처리
                when(userSkillService.getUserSkillWithProfileAndUserAndLock(mentorSkill.getId())).thenReturn(mentorSkill);

                SkillExchangeRequestDto requestDto = new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(), "", date, startTime);
                SkillExchangeDto skillExchangeDto = SkillExchangeDto.from(requestDto);

                // when && then
                assertThatThrownBy(() -> exchangeService.requestSkillExchange(mentor.getId(), skillExchangeDto))
                        .isInstanceOf(SelfExchangeNotAllowedException.class);
            }

            @Test
            @DisplayName("스킬 교환 신청의 종료 시간이 정오를 넘어 발생한 OverExchangeDurationMidnightException")
            public void fail_OverExchangeDurationMidnightException() {
                // given
                // 멘티 조회 Mock 처리
                when(userService.getUserById(mentee.getId())).thenReturn(mentee);

                // 멘토 스킬 조회 Mock 처리
                when(userSkillService.getUserSkillWithProfileAndUserAndLock(mentorSkill.getId())).thenReturn(mentorSkill);

                // 멘토의 가능한 시간 조회 Mock 처리 -> date 날에 [10:00 ~ 12:00], [13:00 ~ 13:30], [23:30 ~ 1:00]
                AvailableScheduleDto schedule1 = new AvailableScheduleDto(
                        date, "SUN", LocalTime.of(10, 0), LocalTime.of(11, 0));
                AvailableScheduleDto schedule2 = new AvailableScheduleDto(
                        date, "SUN", LocalTime.of(13, 0), LocalTime.of(13, 30));
                AvailableScheduleDto schedule3 = new AvailableScheduleDto(
                        date, "SUN", LocalTime.of(23, 30), LocalTime.of(1, 0));
                when(availableScheduleService.getExpandedSchedules(mentor.getId())).thenReturn(List.of(schedule1, schedule2, schedule3));

                // 예약된 현황 조회 Mock 처리 -> date 날에 [10:00 ~ 10:30], [11:00 ~ 12:00]
                SkillExchange exchange1 = createExchange(mentee, mentor, mentorSkill, LocalTime.of(10, 0), LocalTime.of(10, 30));
                SkillExchange exchange2 = createExchange(mentee, mentor, mentorSkill, LocalTime.of(11, 0), LocalTime.of(12, 0));
                when(exchangeRepository.findAllByReceiverIdAndDate(mentor.getId(), date, ExchangeStatus.CANCELED))
                        .thenReturn(List.of(exchange1, exchange2));

                // 정오 넘어서까지 거래가 진행되도록 처리
                LocalTime startTime = LocalTime.of(23, 30);
                SkillExchangeRequestDto requestDto = new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(), "", date, startTime);
                SkillExchangeDto skillExchangeDto = SkillExchangeDto.from(requestDto);

                // when && then
                assertThatThrownBy(() -> exchangeService.requestSkillExchange(mentee.getId(), skillExchangeDto))
                        .isInstanceOf(OverExchangeDurationMidnightException.class);
            }

            @Test
            @DisplayName("멘토가 설정한 가능한 시간 미충족으로 인한 UnavailableExchangeTimeException")
            public void fail_UnavailableExchangeTimeException() {
                // given
                // 멘티 조회 Mock 처리
                when(userService.getUserById(mentee.getId())).thenReturn(mentee);

                // 멘토 스킬 조회 Mock 처리
                when(userSkillService.getUserSkillWithProfileAndUserAndLock(mentorSkill.getId())).thenReturn(mentorSkill);

                // 멘토의 가능한 시간 조회 Mock 처리 -> date 날에 [10:00 ~ 12:00], [13:00 ~ 13:30], [23:30 ~ 1:00]
                AvailableScheduleDto schedule1 = new AvailableScheduleDto(
                        date, "SUN", LocalTime.of(10, 0), LocalTime.of(11, 0));
                AvailableScheduleDto schedule2 = new AvailableScheduleDto(
                        date, "SUN", LocalTime.of(13, 0), LocalTime.of(13, 30));
                AvailableScheduleDto schedule3 = new AvailableScheduleDto(
                        date, "SUN", LocalTime.of(23, 30), LocalTime.of(1, 0));
                when(availableScheduleService.getExpandedSchedules(mentor.getId())).thenReturn(List.of(schedule1, schedule2, schedule3));

                // 예약된 현황 조회 Mock 처리 -> date 날에 [10:00 ~ 10:30], [11:00 ~ 12:00]
                SkillExchange exchange1 = createExchange(mentee, mentor, mentorSkill, LocalTime.of(10, 0), LocalTime.of(10, 30));
                SkillExchange exchange2 = createExchange(mentee, mentor, mentorSkill, LocalTime.of(11, 0), LocalTime.of(12, 0));
                when(exchangeRepository.findAllByReceiverIdAndDate(mentor.getId(), date, ExchangeStatus.CANCELED))
                        .thenReturn(List.of(exchange1, exchange2));

                // 멘토가 설정한 가능한 시간 이외의 요청 처리
                LocalTime startTime = LocalTime.of(12, 30);
                SkillExchangeRequestDto requestDto = new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(), "", date, startTime);
                SkillExchangeDto skillExchangeDto = SkillExchangeDto.from(requestDto);

                // when && then
                assertThatThrownBy(() -> exchangeService.requestSkillExchange(mentee.getId(), skillExchangeDto))
                        .isInstanceOf(UnavailableExchangeTimeException.class);
            }

            @Test
            @DisplayName("이미 예약된 시간으로 인한 AlreadyBookedExchangeTimeException")
            public void fail_AlreadyBookedExchangeTimeException() {
                // given
                // 멘티 조회 Mock 처리
                when(userService.getUserById(mentee.getId())).thenReturn(mentee);

                // 멘토 스킬 조회 Mock 처리
                when(userSkillService.getUserSkillWithProfileAndUserAndLock(mentorSkill.getId())).thenReturn(mentorSkill);

                // 멘토의 가능한 시간 조회 Mock 처리 -> date 날에 [10:00 ~ 12:00], [13:00 ~ 13:30], [23:30 ~ 1:00]
                AvailableScheduleDto schedule1 = new AvailableScheduleDto(
                        date, "SUN", LocalTime.of(10, 0), LocalTime.of(11, 0));
                AvailableScheduleDto schedule2 = new AvailableScheduleDto(
                        date, "SUN", LocalTime.of(13, 0), LocalTime.of(13, 30));
                AvailableScheduleDto schedule3 = new AvailableScheduleDto(
                        date, "SUN", LocalTime.of(23, 30), LocalTime.of(1, 0));
                when(availableScheduleService.getExpandedSchedules(mentor.getId())).thenReturn(List.of(schedule1, schedule2, schedule3));

                // 예약된 현황 조회 Mock 처리 -> date 날에 [10:00 ~ 10:30], [11:00 ~ 12:00]
                SkillExchange exchange1 = createExchange(mentee, mentor, mentorSkill, LocalTime.of(10, 0), LocalTime.of(10, 30));
                SkillExchange exchange2 = createExchange(mentee, mentor, mentorSkill, LocalTime.of(11, 0), LocalTime.of(12, 0));
                when(exchangeRepository.findAllByReceiverIdAndDate(mentor.getId(), date, ExchangeStatus.CANCELED))
                        .thenReturn(List.of(exchange1, exchange2));

                // 멘토가 설정한 가능한 시간 이외의 요청 처리
                LocalTime startTime = LocalTime.of(10, 0);
                SkillExchangeRequestDto requestDto = new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(), "", date, startTime);
                SkillExchangeDto skillExchangeDto = SkillExchangeDto.from(requestDto);

                // when && then
                assertThatThrownBy(() -> exchangeService.requestSkillExchange(mentee.getId(), skillExchangeDto))
                        .isInstanceOf(AlreadyBookedExchangeTimeException.class);
            }

            @Test
            @DisplayName("멘티의 크레딧 부족으로 인한 NotEnoughCreditException")
            public void success() {
                // given
                // 멘티 조회 Mock 처리
                when(userService.getUserById(mentee.getId())).thenReturn(mentee);

                // 멘토 스킬 조회 Mock 처리
                when(userSkillService.getUserSkillWithProfileAndUserAndLock(mentorSkill.getId())).thenReturn(mentorSkill);

                // 멘티의 크레딧 잔액이 요청 금액보다 부족 검증 Mock
                int amount = mentorSkill.getExchangeDuration() / CREDIT_EXCHANGE_RATE_MINUTES;
                doThrow(new NotEnoughCreditException()).when(creditService).validateAvailableBalance(mentee.getId(), amount);


                SkillExchangeRequestDto requestDto = new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(), "", date, startTime);
                SkillExchangeDto skillExchangeDto = SkillExchangeDto.from(requestDto);

                // when && then
                assertThatThrownBy(() -> exchangeService.requestSkillExchange(mentee.getId(), skillExchangeDto))
                        .isInstanceOf(NotEnoughCreditException.class);
            }
        }
    }

    @Test
    @DisplayName("스킬 거래 요청 조회 - 보낸 요청")
    public void getSentRequests() {
        // given
        Long userId = 1L;
        Long cursorId = null;
        int size = 10;
        Pageable pageable = PageRequest.of(0, size);
        LocalDateTime now = LocalDateTime.now();
        LocalDate exchangeDate = LocalDate.of(2024, 1, 25);
        LocalTime exchangeTime = LocalTime.of(12, 0);

        // 테스트용 Query 데이터 생성 (isRead가 false인 데이터 포함)
        SkillExchangeDetailQuery queryResult = new SkillExchangeDetailQuery(
                10L, userId, 2L, 1L, 100L, "url", "상대방", "Java",
                ExchangeStatus.PENDING, 2, "메시지", now,
                exchangeDate, exchangeTime, 60, false
        );

        Slice<SkillExchangeDetailQuery> slice = new SliceImpl<>(List.of(queryResult), pageable, false);

        // Mock 설정
        when(exchangeRepository.findAllByRequesterIdWithReceiver(userId, cursorId, pageable))
                .thenReturn(slice);

        // when
        SkillExchangeDetailsResponseDto result = exchangeService.getSentRequests(userId, cursorId, size);

        // then
        // 1. Repository의 조회 메서드가 호출되었는지 확인
        verify(exchangeRepository).findAllByRequesterIdWithReceiver(userId, cursorId, pageable);

        // 2. 벌크 업데이트 메서드가 호출되었는지 확인
        verify(exchangeRepository).bulkUpdateRequesterReadStatus(userId);

        // 3. 응답 데이터 검증
        assertThat(result.getContents()).hasSize(1);

        SkillExchangeDetailDto dto = result.getContents().get(0);
        assertThat(dto.getSkillExchangeId()).isEqualTo(queryResult.skillExchangeId());
        assertThat(dto.getTargetUserId()).isEqualTo(queryResult.targetUserId());
        assertThat(dto.getSkillId()).isEqualTo(queryResult.skillId());
        assertThat(dto.getChatRoomId()).isEqualTo(queryResult.chatRoomId());
        assertThat(dto.getTargetProfileImageUrl()).isEqualTo(queryResult.targetProfileImageUrl());
        assertThat(dto.getTargetNickname()).isEqualTo(queryResult.targetNickname());
        assertThat(dto.getSkillName()).isEqualTo(queryResult.skillName());

        assertThat(dto.getExchangeStatus()).isEqualTo(queryResult.exchangeStatus().getDescription());
        assertThat(dto.getCreditPrice()).isEqualTo(queryResult.creditPrice());
        assertThat(dto.getMessage()).isEqualTo(queryResult.message());

        // LocalDateTime 변환 검증 (createdAt)
        assertThat(dto.getRequestedDate()).isEqualTo(now.toLocalDate());

        // LocalDate + LocalTime 결합 검증 (atTime 로직 확인)
        assertThat(dto.getExchangeDateTime()).isEqualTo(exchangeDate.atTime(exchangeTime));
        assertThat(dto.getExchangeDuration()).isEqualTo(queryResult.exchangeDuration());
        // isRead(false) -> isNew(true) 변환 로직 검증
        assertThat(dto.isNew()).isTrue();
    }

    @Test
    @DisplayName("스킬 거래 요청 조회 - 보낸 요청 (데이터가 없을 경우 빈 목록을 반환하고 벌크 업데이트 실행)")
    void getSentRequests_Empty() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Slice<SkillExchangeDetailQuery> emptySlice = new SliceImpl<>(List.of(), pageable, false);

        when(exchangeRepository.findAllByRequesterIdWithReceiver(any(), any(), any()))
                .thenReturn(emptySlice);

        // when
        SkillExchangeDetailsResponseDto result = exchangeService.getSentRequests(userId, null, 10);

        // then
        assertThat(result.getContents()).isEmpty();
        assertThat(result.isHasNext()).isFalse();
        verify(exchangeRepository).bulkUpdateRequesterReadStatus(userId);
    }

    @Test
    @DisplayName("스킬 거래 요청 조회 - 받은 요청")
    public void getReceivedRequests() {
        // given
        Long userId = 1L;
        Long cursorId = null;
        int size = 10;
        Pageable pageable = PageRequest.of(0, size);
        LocalDateTime now = LocalDateTime.now();
        LocalDate exchangeDate = LocalDate.of(2024, 1, 25);
        LocalTime exchangeTime = LocalTime.of(12, 0);

        // 테스트용 Query 데이터 생성 (isRead가 false인 데이터 포함)
        SkillExchangeDetailQuery queryResult = new SkillExchangeDetailQuery(
                10L, userId, 2L, 1L, 100L, "url", "상대방", "Java",
                ExchangeStatus.PENDING, 2, "메시지", now,
                exchangeDate, exchangeTime, 60, false
        );

        Slice<SkillExchangeDetailQuery> slice = new SliceImpl<>(List.of(queryResult), pageable, false);

        // Mock 설정
        when(exchangeRepository.findAllByReceiverIdWithRequester(userId, cursorId, pageable))
                .thenReturn(slice);

        // when
        SkillExchangeDetailsResponseDto result = exchangeService.getReceivedRequests(userId, cursorId, size);

        // then
        // 1. Repository의 조회 메서드가 호출되었는지 확인
        verify(exchangeRepository).findAllByReceiverIdWithRequester(userId, cursorId, pageable);

        // 2. 벌크 업데이트 메서드가 호출되었는지 확인
        verify(exchangeRepository).bulkUpdateReceiverReadStatus(userId);

        // 3. 응답 데이터 검증
        assertThat(result.getContents()).hasSize(1);

        SkillExchangeDetailDto dto = result.getContents().get(0);
        assertThat(dto.getSkillExchangeId()).isEqualTo(queryResult.skillExchangeId());
        assertThat(dto.getTargetUserId()).isEqualTo(queryResult.targetUserId());
        assertThat(dto.getSkillId()).isEqualTo(queryResult.skillId());
        assertThat(dto.getChatRoomId()).isEqualTo(queryResult.chatRoomId());
        assertThat(dto.getTargetProfileImageUrl()).isEqualTo(queryResult.targetProfileImageUrl());
        assertThat(dto.getTargetNickname()).isEqualTo(queryResult.targetNickname());
        assertThat(dto.getSkillName()).isEqualTo(queryResult.skillName());

        assertThat(dto.getExchangeStatus()).isEqualTo(queryResult.exchangeStatus().getDescription());
        assertThat(dto.getCreditPrice()).isEqualTo(queryResult.creditPrice());
        assertThat(dto.getMessage()).isEqualTo(queryResult.message());

        // LocalDateTime 변환 검증 (createdAt)
        assertThat(dto.getRequestedDate()).isEqualTo(now.toLocalDate());

        // LocalDate + LocalTime 결합 검증 (atTime 로직 확인)
        assertThat(dto.getExchangeDateTime()).isEqualTo(exchangeDate.atTime(exchangeTime));
        assertThat(dto.getExchangeDuration()).isEqualTo(queryResult.exchangeDuration());
        // isRead(false) -> isNew(true) 변환 로직 검증
        assertThat(dto.isNew()).isTrue();
    }

    @Nested
    @DisplayName("스킬 거래 수락")
    class AcceptSkillExchange {
        private User receiver = createUser();
        private User requester = createUser();
        private UserSkill receiverSkill = createUserSkill(60);
        private LocalTime start = LocalTime.now();
        private LocalTime end = LocalTime.now().plusMinutes(60);

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("성공")
            public void success() {
                // given
                SkillExchange skillExchange = createExchange(requester, receiver, receiverSkill, start, end);
                // skillExchange 조회 및 검증 Mock
                when(exchangeRepository.findByIdWithReceiver(skillExchange.getId())).thenReturn(Optional.of(skillExchange));

                // when
                SkillExchangeResponseDto sut = exchangeService.acceptSkillExchange(receiver.getId(), skillExchange.getId());

                // then
                assertThat(skillExchange.isRequesterRead()).isFalse();
                assertThat(sut.getExchangeStatus()).isEqualTo(ExchangeStatus.ACCEPTED.getDescription());
                assertThat(sut.getSkillExchangeId()).isEqualTo(skillExchange.getId());
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("receiverId로 스킬 거래가 존재 하지 않음 ExchangeNotFoundException")
            public void fail_ExchangeNotFoundException() {
                // given
                SkillExchange skillExchange = createExchange(requester, receiver, receiverSkill, start, end);
                // skillExchange 조회 및 검증 Mock
                when(exchangeRepository.findByIdWithReceiver(skillExchange.getId())).thenReturn(Optional.empty());

                // when && then
                assertThatThrownBy(() -> exchangeService.acceptSkillExchange(receiver.getId(), skillExchange.getId()))
                        .isInstanceOf(ExchangeNotFoundException.class);
            }

            @Test
            @DisplayName("해당 스킬 거래가 receiver의 거래가 아님 ExchangeAccessDeniedException")
            public void fail_ExchangeAccessDeniedException() {
                // given
                User otherUser = createUser();
                SkillExchange skillExchange = createExchange(requester, otherUser, receiverSkill, start, end);
                // skillExchange 조회 및 검증 Mock
                when(exchangeRepository.findByIdWithReceiver(skillExchange.getId())).thenReturn(Optional.of(skillExchange));

                // when && then
                assertThatThrownBy(() -> exchangeService.acceptSkillExchange(receiver.getId(), skillExchange.getId()))
                        .isInstanceOf(ExchangeAccessDeniedException.class);
            }

            @Test
            @DisplayName("스킬 거래 상태가 대기중이 아니라 InvalidExchangeStatus")
            public void fail_InvalidExchangeStatus() {
                // given
                SkillExchange skillExchange = createExchange(requester, receiver, receiverSkill, start, end);
                // skillExchange 조회 및 검증 Mock
                when(exchangeRepository.findByIdWithReceiver(skillExchange.getId())).thenReturn(Optional.of(skillExchange));
                skillExchange.updateExchangeStatus(ExchangeStatus.ACCEPTED);

                // when && then
                assertThatThrownBy(() -> exchangeService.acceptSkillExchange(receiver.getId(), skillExchange.getId()))
                        .isInstanceOf(InvalidExchangeStatusException.class);
            }
        }
    }

    @Nested
    @DisplayName("스킬 거래 거절")
    class RejectSkillExchange {
        private User receiver = createUser();
        private User requester = createUser();
        private UserSkill receiverSkill = createUserSkill(60);
        private LocalTime start = LocalTime.now();
        private LocalTime end = LocalTime.now().plusMinutes(60);

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("성공")
            public void success() {
                // given
                SkillExchange skillExchange = createExchange(requester, receiver, receiverSkill, start, end);
                // skillExchange 조회 및 검증 Mock
                when(exchangeRepository.findByIdWithReceiver(skillExchange.getId())).thenReturn(Optional.of(skillExchange));

                // when
                SkillExchangeResponseDto sut = exchangeService.rejectSkillExchange(receiver.getId(), skillExchange.getId());

                // then
                assertThat(skillExchange.isRequesterRead()).isFalse();
                assertThat(sut.getExchangeStatus()).isEqualTo(ExchangeStatus.REJECTED.getDescription());
                assertThat(sut.getSkillExchangeId()).isEqualTo(skillExchange.getId());
                verify(creditService).refundCreditForExchange(
                        eq(skillExchange),
                        eq(SupplyType.ADD),
                        eq(HistoryType.EXCHANGE_REJECTED));
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("receiverId로 스킬 거래가 존재 하지 않음 ExchangeNotFoundException")
            public void fail_ExchangeNotFoundException() {
                // given
                SkillExchange skillExchange = createExchange(requester, receiver, receiverSkill, start, end);
                // skillExchange 조회 및 검증 Mock
                when(exchangeRepository.findByIdWithReceiver(skillExchange.getId())).thenReturn(Optional.empty());

                // when && then
                assertThatThrownBy(() -> exchangeService.rejectSkillExchange(receiver.getId(), skillExchange.getId()))
                        .isInstanceOf(ExchangeNotFoundException.class);
            }

            @Test
            @DisplayName("해당 스킬 거래가 receiver의 거래가 아님 ExchangeAccessDeniedException")
            public void fail_ExchangeAccessDeniedException() {
                // given
                User otherUser = createUser();
                SkillExchange skillExchange = createExchange(requester, otherUser, receiverSkill, start, end);
                // skillExchange 조회 및 검증 Mock
                when(exchangeRepository.findByIdWithReceiver(skillExchange.getId())).thenReturn(Optional.of(skillExchange));

                // when && then
                assertThatThrownBy(() -> exchangeService.rejectSkillExchange(receiver.getId(), skillExchange.getId()))
                        .isInstanceOf(ExchangeAccessDeniedException.class);
            }

            @Test
            @DisplayName("스킬 거래 상태가 대기중이 아니라 InvalidExchangeStatus")
            public void fail_InvalidExchangeStatus() {
                // given
                SkillExchange skillExchange = createExchange(requester, receiver, receiverSkill, start, end);
                // skillExchange 조회 및 검증 Mock
                when(exchangeRepository.findByIdWithReceiver(skillExchange.getId())).thenReturn(Optional.of(skillExchange));
                skillExchange.updateExchangeStatus(ExchangeStatus.ACCEPTED);

                // when && then
                assertThatThrownBy(() -> exchangeService.rejectSkillExchange(receiver.getId(), skillExchange.getId()))
                        .isInstanceOf(InvalidExchangeStatusException.class);
            }
        }
    }

    @Nested
    @DisplayName("스킬 거래 취소")
    class CancelSkillExchange {
        private User receiver = createUser();
        private User requester = createUser();
        private UserSkill receiverSkill = createUserSkill(60);


        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {

            @Test
            @DisplayName("멘토는 수락된 거래만 취소 가능")
            public void success_mentor() {
                // given
                // 취소 기한 통과를 위해 내일 날짜로 설정
                SkillExchange skillExchange = createExchange(requester, receiver, receiverSkill,
                        LocalTime.of(14, 0), LocalTime.of(15, 0));
                ReflectionTestUtils.setField(skillExchange, "scheduledDate", LocalDate.now().plusDays(1));
                skillExchange.updateExchangeStatus(ExchangeStatus.ACCEPTED);

                when(exchangeRepository.findById(skillExchange.getId())).thenReturn(Optional.of(skillExchange));

                // when
                SkillExchangeResponseDto result = exchangeService.cancelSkillExchange(receiver.getId(), skillExchange.getId());

                // then
                assertThat(skillExchange.getExchangeStatus()).isEqualTo(ExchangeStatus.CANCELED);
                assertThat(skillExchange.isRequesterRead()).isFalse();
                verify(creditService).refundCreditForExchange(eq(skillExchange), eq(SupplyType.ADD), eq(HistoryType.EXCHANGE_CANCELED));
                assertThat(result.getExchangeStatus()).isEqualTo(ExchangeStatus.CANCELED.getDescription());
            }


            @Test
            @DisplayName("멘티는 대기중인 거래를 취소 할 수 있음")
            public void success_mentee_Pending() {
                // given
                SkillExchange skillExchange = createExchange(requester, receiver, receiverSkill,
                        LocalTime.of(14, 0), LocalTime.of(15, 0));
                ReflectionTestUtils.setField(skillExchange, "scheduledDate", LocalDate.now().plusDays(1));

                when(exchangeRepository.findById(skillExchange.getId())).thenReturn(Optional.of(skillExchange));

                // when
                exchangeService.cancelSkillExchange(requester.getId(), skillExchange.getId());

                // then
                assertThat(skillExchange.getExchangeStatus()).isEqualTo(ExchangeStatus.CANCELED);
                assertThat(skillExchange.isReceiverRead()).isFalse();
                verify(creditService).refundCreditForExchange(any(), any(), any());
            }

            @Test
            @DisplayName("멘티는 수락된 거래를 취소할 수 있음")
            public void success_mentee_Accepted() {
                // given
                SkillExchange skillExchange = createExchange(requester, receiver, receiverSkill,
                        LocalTime.of(14, 0), LocalTime.of(15, 0));
                ReflectionTestUtils.setField(skillExchange, "scheduledDate", LocalDate.now().plusDays(1));
                skillExchange.updateExchangeStatus(ExchangeStatus.ACCEPTED);

                when(exchangeRepository.findById(skillExchange.getId())).thenReturn(Optional.of(skillExchange));

                // when
                exchangeService.cancelSkillExchange(requester.getId(), skillExchange.getId());

                // then
                assertThat(skillExchange.getExchangeStatus()).isEqualTo(ExchangeStatus.CANCELED);
                verify(creditService).refundCreditForExchange(any(), any(), any());
            }
        }
        @Nested
        @DisplayName("실패 케이스")
        class FailCases {

            @Test
            @DisplayName("거래와 관련 없는 제3자가 취소를 시도하면 ExchangeAccessDeniedException")
            public void fail_ExchangeAccessDenied() {
                // given
                SkillExchange skillExchange = createExchange(requester, receiver, receiverSkill,
                        LocalTime.of(14, 0), LocalTime.of(15, 0));
                Long strangerId = 999L;

                when(exchangeRepository.findById(skillExchange.getId())).thenReturn(Optional.of(skillExchange));

                // when & then
                assertThatThrownBy(() -> exchangeService.cancelSkillExchange(strangerId, skillExchange.getId()))
                        .isInstanceOf(ExchangeAccessDeniedException.class);
            }

            @Test
            @DisplayName("거래 당일 취소를 시도하면 ExchangeCancelNotAllowedException")
            public void fail_ExchangeCancelNotAllowed() {
                // given
                SkillExchange skillExchange = createExchange(requester, receiver, receiverSkill,
                        LocalTime.of(14, 0), LocalTime.of(15, 0));
                // 오늘 날짜로 강제 설정 (validateCancelDeadline 통과 불가)
                ReflectionTestUtils.setField(skillExchange, "scheduledDate", LocalDate.now());

                when(exchangeRepository.findById(skillExchange.getId())).thenReturn(Optional.of(skillExchange));

                // when & then
                assertThatThrownBy(() -> exchangeService.cancelSkillExchange(requester.getId(), skillExchange.getId()))
                        .isInstanceOf(ExchangeCancelNotAllowedException.class);
            }

            @Test
            @DisplayName("멘토(수락자)가 아직 수락하지 않은(PENDING) 상태에서 취소를 시도하면 InvalidExchangeStatusException")
            public void fail_Mentor_Cancel_Pending() {
                // given
                SkillExchange skillExchange = createExchange(requester, receiver, receiverSkill,
                        LocalTime.of(14, 0), LocalTime.of(15, 0));
                ReflectionTestUtils.setField(skillExchange, "scheduledDate", LocalDate.now().plusDays(1));
                // 상태: PENDING

                when(exchangeRepository.findById(skillExchange.getId())).thenReturn(Optional.of(skillExchange));

                // when & then
                assertThatThrownBy(() -> exchangeService.cancelSkillExchange(receiver.getId(), skillExchange.getId()))
                        .isInstanceOf(InvalidExchangeStatusException.class)
                        .hasMessageContaining("수락된 거래만 취소가 가능합니다");
            }

            @Test
            @DisplayName("이미 거절되거나 취소된 거래를 다시 취소하려고 하면 InvalidExchangeStatusException")
            public void fail_Already_Processed_Exchange() {
                // given
                SkillExchange skillExchange = createExchange(requester, receiver, receiverSkill,
                        LocalTime.of(14, 0), LocalTime.of(15, 0));
                ReflectionTestUtils.setField(skillExchange, "scheduledDate", LocalDate.now().plusDays(1));
                skillExchange.cancel(); // 이미 취소됨

                when(exchangeRepository.findById(skillExchange.getId())).thenReturn(Optional.of(skillExchange));

                // when & then
                assertThatThrownBy(() -> exchangeService.cancelSkillExchange(requester.getId(), skillExchange.getId()))
                        .isInstanceOf(InvalidExchangeStatusException.class);
            }
        }
    }

    private UserProfile createUserProfile(User user, List<UserSkill> userSkill) {
        UserProfile userProfile = UserProfile.create(user,
                "introduction",
                "description",
                ExchangeType.OFFLINE,
                PreferredRegion.CHUNGCHEONG,
                "location");
        for (UserSkill skill : userSkill) {
            userProfile.addUserSkill(skill);
        }
        ReflectionTestUtils.setField(userProfile, "id", profileId++);
        return userProfile;
    }

    private User createUser() {
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao" + userId,
                "email@example.com" + userId,
                "name" + userId,
                "https://image",
                "nickname" + userId);
        ReflectionTestUtils.setField(user, "id", userId++);
        return user;
    }

    private UserSkill createUserSkill(int exchangeDuration) {
        UserSkill userSkill = UserSkill.create(
                null,
                "skillName",
                SkillLevel.LOW,
                "description",
                exchangeDuration,
                true);
        ReflectionTestUtils.setField(userSkill, "id", userSkillId++);
        return userSkill;
    }

    private UserSkill createUnVisibleUserSkill(int exchangeDuration) {
        UserSkill userSkill = UserSkill.create(
                null,
                "skillName",
                SkillLevel.LOW,
                "description",
                exchangeDuration,
                false);
        ReflectionTestUtils.setField(userSkill, "id", userSkillId++);
        return userSkill;
    }

    private Credit createCredit(User user, int amount) {
        Credit credit = Credit.create(user, amount);
        ReflectionTestUtils.setField(credit, "id", creditId++);
        return credit;
    }

    private CreditHistory createExchangeRequestHistory(User mentee, User mentor, SkillExchange exchange,
                                                       int amount, int balanceAfter) {
        CreditHistory history = CreditHistory.createSkillExchange(
                mentee,
                mentor,
                exchange,
                SupplyType.USE,
                amount,
                balanceAfter,
                HistoryType.EXCHANGE_REQUEST
        );
        ReflectionTestUtils.setField(history, "id", historyId++);
        return history;
    }

    private SkillExchange createExchange(User requester, User receiverUser, UserSkill receiverSkill,
                                         LocalTime starTime, LocalTime endTime) {

        SkillExchange skillExchange = SkillExchange.create(
                requester, receiverUser, receiverSkill, LocalDate.of(2026, 2, 4),
                starTime, endTime, "message");
        ReflectionTestUtils.setField(skillExchange, "id", exchangeId++);
        return skillExchange;
    }

}