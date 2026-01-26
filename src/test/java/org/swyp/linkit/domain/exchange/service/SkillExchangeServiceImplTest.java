package org.swyp.linkit.domain.exchange.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.swyp.linkit.domain.exchange.dto.response.AvailableDatesResponseDto;
import org.swyp.linkit.domain.exchange.dto.response.AvailableSlotsResponseDto;
import org.swyp.linkit.domain.exchange.dto.response.SlotDto;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.SkillExchangeRepository;
import org.swyp.linkit.domain.user.dto.AvailableScheduleDto;
import org.swyp.linkit.domain.user.entity.*;
import org.swyp.linkit.domain.user.service.AvailableScheduleService;
import org.swyp.linkit.domain.user.service.UserService;
import org.swyp.linkit.domain.user.service.UserSkillService;
import org.swyp.linkit.global.error.exception.MentorNotFoundException;
import org.swyp.linkit.global.error.exception.ScheduleNotFoundException;
import org.swyp.linkit.global.error.exception.UserNotFoundException;
import org.swyp.linkit.global.error.exception.UserSkillNotFoundException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

    @InjectMocks
    SkillExchangeServiceImpl exchangeService;

    private Long userId = 1L;
    private Long userSkillId = 1L;
    private Long exchangeId = 1L;
    private Long profileId = 1L;

    @Nested
    @DisplayName("멘토의 거래 가능 날짜 조회 (getAvailableDates)")
    class GetAvailableDates {
        private final User mentorUser = createUser();
        private final String month = "2026-02";

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCase {
            @Test
            @DisplayName("멘토의 거래 가능 날짜를 반환")
            public void success() {
                // given
                // mentor 조회 Mock 처리
                when(userService.getUserById(mentorUser.getId())).thenReturn(mentorUser);

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

                when(availableScheduleService.getExpandedSchedules(mentorUser.getId()))
                        .thenReturn(List.of(schedule1, schedule2, schedule3));
                // when
                AvailableDatesResponseDto response = exchangeService.getAvailableDates(mentorUser.getId(), month);

                // then
                // 중복 제거로 인해 2
                assertThat(response.getAvailableDates()).hasSize(2);
                assertThat(response.getAvailableDates().stream().allMatch(date -> date.startsWith(month))).isTrue();

                verify(userService).getUserById(mentorUser.getId());
                verify(availableScheduleService).getExpandedSchedules(mentorUser.getId());
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
                        .when(userService).getUserById(mentorUser.getId());

                // when && then
                assertThatThrownBy(() -> exchangeService.getAvailableDates(mentorUser.getId(), month))
                        .isInstanceOf(MentorNotFoundException.class);
            }

            @Test
            @DisplayName("해당 월에 가능한 스케줄이 존재하지 않아 ScheduleNotFoundException 발생")
            public void fail_ScheduleNotFoundException() {
                // given
                // mentor 조회 Mock 처리
                when(userService.getUserById(mentorUser.getId())).thenReturn(mentorUser);
                // 3개월치 데이터 빈 데이터 처리
                when(availableScheduleService.getExpandedSchedules(mentorUser.getId()))
                        .thenReturn(List.of());

                // when && then
                assertThatThrownBy(() -> exchangeService.getAvailableDates(mentorUser.getId(), month))
                        .isInstanceOf(ScheduleNotFoundException.class);

            }
        }
    }

    @Nested
    @DisplayName("멘토의 날짜 별 거래 가능 시간 조회 (getAvailableSlots)")
    class GetAvailableSlots {
        private final User mentorUser = createUser();
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
                SkillExchange exchange1 = createExchange(mentorUser, receiverSkill1,
                        LocalTime.of(10, 0), LocalTime.of(11, 0));
                SkillExchange exchange2 = createExchange(mentorUser, receiverSkill2,
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
                assertThat(filteredDto.get(0).getTime()).isEqualTo("11:00");
                assertThat(filteredDto.get(1).getTime()).isEqualTo("11:30");
                assertThat(filteredDto.get(2).getTime()).isEqualTo("16:30");
                assertThat(filteredDto.get(3).getTime()).isEqualTo("17:30");

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
            @DisplayName("mentor의 정보와 userSkill이 일치하지 않아 MentorNotFoundException 발생")
            public void fail_MentorNotFoundException() {
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
                        .isInstanceOf(MentorNotFoundException.class)
                        .hasMessageContaining("해당 멘토가 보유한 스킬이 아닙니다.");
            }
        }
    }

    private UserProfile createUserProfile(User user, List<UserSkill> userSkill){
        UserProfile userProfile = UserProfile.create(user,
                "introduction",
                "description",
                ExchangeType.OFFLINE,
                PreferredRegion.CHUNGCHEONG,
                "location");
        for(UserSkill skill : userSkill){
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

    private SkillExchange createExchange(User receiverUser, UserSkill receiverSkill,
                                         LocalTime starTime, LocalTime endTime) {

        SkillExchange skillExchange = SkillExchange.create(
                null, receiverUser, receiverSkill, LocalDate.of(2026, 2, 4),
               starTime, endTime, "message");
        ReflectionTestUtils.setField(skillExchange, "id", exchangeId++);
        return skillExchange;
    }

}