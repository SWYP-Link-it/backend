package org.swyp.linkit.domain.exchange.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.TestRedisConfig;
import org.swyp.linkit.domain.chat.entity.ChatRoom;
import org.swyp.linkit.domain.chat.repository.ChatRoomRepository;
import org.swyp.linkit.domain.credit.entity.Credit;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.repository.CreditHistoryRepository;
import org.swyp.linkit.domain.credit.repository.CreditRepository;
import org.swyp.linkit.domain.exchange.dto.SkillExchangeDto;
import org.swyp.linkit.domain.exchange.dto.request.SkillExchangeRequestDto;
import org.swyp.linkit.domain.exchange.dto.response.*;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.SkillExchangeRepository;
import org.swyp.linkit.domain.settlement.entity.Settlement;
import org.swyp.linkit.domain.settlement.entity.SettlementStatus;
import org.swyp.linkit.domain.settlement.repository.SettlementRepository;
import org.swyp.linkit.domain.user.entity.*;
import org.swyp.linkit.domain.user.repository.*;
import org.swyp.linkit.global.error.exception.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.time.DayOfWeek.MONDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestRedisConfig.class)
@ActiveProfiles("test")
@SpringBootTest(properties = "scheduler.enabled=false")
@DisplayName("SkillExchangeService 통합 테스트")
public class SkillExchangeServiceImplIntegrationTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private SkillExchangeService skillExchangeService;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private UserSkillRepository userSkillRepository;
    @Autowired
    private SkillCategoryRepository skillCategoryRepository;
    @Autowired
    private SkillExchangeRepository skillExchangeRepository;
    @Autowired
    private CreditRepository creditRepository;
    @Autowired
    private CreditHistoryRepository creditHistoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AvailableScheduleRepository availableScheduleRepository;
    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Transactional
    @Nested
    @DisplayName("2일 뒤 ~ 3달 까지의 멘토의 거래 가능 날짜 조회.")
    class GetAvailableDates {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("2일 뒤 ~ 3달 까지의 멘토의 거래 가능 날짜를 조회한다. 날짜는 정렬, 중복 제거 되어야한다.")
            public void getAvailableDates() {
                // given
                User mentor = createSavedUser();
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill mentorSkill = createUserSkill(skillCategory, 30);
                createSavedUserProfile(mentor, mentorSkill);
                createSavedWeekDaysSchedule(mentor);

                String yearMonth = YearMonth.now().toString();
                Long mentorId = mentor.getId();

                em.flush();
                em.clear();

                // when
                AvailableDatesResponseDto availableDates = skillExchangeService.getAvailableDates(mentorId, yearMonth);

                // then
                // 요청한 연월 검증
                assertThat(availableDates).isNotNull();
                assertThat(availableDates.getYearMonth()).isEqualTo(yearMonth);

                // 가능 날짜 검증
                List<String> resultDates = availableDates.getAvailableDates();
                assertThat(resultDates).isNotEmpty();
                assertThat(resultDates).allMatch(date -> date.startsWith(yearMonth));
                // 정렬 확인
                assertThat(resultDates).isSorted();
                // 중복 확인
                assertThat(resultDates).doesNotHaveDuplicates();
                // 2일 뒤 날짜부터 들어있는지 검증
                String sampleDate = resultDates.get(0);
                LocalDate firstDate = LocalDate.parse(sampleDate);
                assertThat(firstDate).isAfter(LocalDate.now().plusDays(1));

                // 출력 용
                List<String> availableDates1 = availableDates.getAvailableDates();
                for (String s : availableDates1) {
                    System.out.println(s);
                }
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("멘토가 존재하지 않아 MentorNotFoundException 발생.")
            public void fail_MentorNotFoundException() {
                // given
                Long nonExistMentorId = Long.MAX_VALUE;
                String yearMonth = YearMonth.now().toString();

                em.flush();
                em.clear();

                // when & then
                assertThatThrownBy(() -> skillExchangeService.getAvailableDates(nonExistMentorId, yearMonth))
                        .isInstanceOf(MentorNotFoundException.class);
            }

            @Test
            @DisplayName("해당 월에 등록된 멘토의 스케줄이 없으면 ScheduleNotFoundException 발생.")
            public void fail_ScheduleNotFoundException() {
                // given
                User mentor = createSavedUser();
                String nextYearMonth = YearMonth.now().plusYears(10).toString();

                em.flush();
                em.clear();

                // when & then
                assertThatThrownBy(() -> skillExchangeService.getAvailableDates(mentor.getId(), nextYearMonth))
                        .isInstanceOf(ScheduleNotFoundException.class);
            }
        }
    }

    @Transactional
    @Nested
    @DisplayName("멘토의 날짜 별 거래 가능 시간 조회")
    class GetAvailableSlots {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("멘토의 가능 시간 중 이미 예약된 슬롯을 제외하고, 거래 시간만큼 가능한 슬롯만 true로 반환한다.")
            public void success() {
                  // given
                int exchangeDuration = 90;
                User mentee = createSavedUser();
                User mentor = createSavedUser();
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill mentorSkill = createUserSkill(skillCategory, exchangeDuration);
                createSavedUserProfile(mentor, mentorSkill);
                createSavedWeekDaysSchedule(mentor);

                Long mentorId = mentor.getId();
                Long mentorSkillId = mentorSkill.getId();
                // 월요일로 고정
                LocalDate targetDate = LocalDate.now().plusWeeks(1).with(MONDAY);

                // 멘토의 예약된 거래 생성 (10:00 ~ 11:00)
                LocalTime bookedStart = LocalTime.of(10, 0);
                LocalTime bookedEnd = LocalTime.of(11, 0);
                createSavedExchange(mentee, mentor, mentorSkill, targetDate, bookedStart, bookedEnd);

                em.flush();
                em.clear();

                // when
                AvailableSlotsResponseDto availableSlots = skillExchangeService.getAvailableSlots(mentorId, mentorSkillId, targetDate);

                // then
                // 요청한 연월 검증
                assertThat(availableSlots.getDate()).isEqualTo(targetDate.toString());
                List<SlotDto> slots = availableSlots.getSlots();

                // 10:00, 10:30  슬롯은 이미 예약되어 slot에 포합되지 않는다.
                assertThat(findSlot(slots, 10, 0)).isFalse();
                assertThat(findSlot(slots, 10, 30)).isFalse();

                // 11:00, 11:30 슬롯은 90분 확보가 안되기에 slot에 포합되지 않는다.
                assertThat(findSlot(slots, 11, 0)).isFalse();
                assertThat(findSlot(slots, 11, 30)).isFalse();

                // 18:00, 18:30 슬롯은 90분 확보가 되어 slot에 포함된다.
                assertThat(findSlot(slots, 18, 0)).isTrue();
                assertThat(findSlot(slots, 18, 30)).isTrue();

                // 19:00, 19:30 슬롯은 90분 확보가 안되기에 slot에 포함되지 않는다.
                assertThat(findSlot(slots, 19, 0)).isFalse();
                assertThat(findSlot(slots, 19, 30)).isFalse();

                slots.forEach(System.out::println);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("멘토의 스킬을 찾을 수 없어 UserSkillNotFoundException 발생.")
            public void fail_UserSkillNotFoundException() {
                // given
                User mentor = createSavedUser();
                Long nonExistSkillId = Long.MAX_VALUE;
                LocalDate date = LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.MONDAY);

                // when & then
                assertThatThrownBy(() ->
                        skillExchangeService.getAvailableSlots(mentor.getId(), nonExistSkillId, date))
                        .isInstanceOf(UserSkillNotFoundException.class);
            }

            @Test
            @DisplayName("스킬의 소유자와 입력받은 멘토가 다르면 SkillMentorMissMatchException 발생.")
            public void fail_SkillMentorMissMatchException() {
                // given
                User mentor = createSavedUser();
                User wrongMentor = createSavedUser();

                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill mentorSkill = createUserSkill(skillCategory, 60);
                createSavedUserProfile(mentor, mentorSkill);

                LocalDate date = LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.MONDAY);

                // when & then
                assertThatThrownBy(() ->
                        skillExchangeService.getAvailableSlots(wrongMentor.getId(), mentorSkill.getId(), date))
                        .isInstanceOf(SkillMentorMissMatchException.class);
            }
        }
    }

    @Transactional
    @Nested
    @DisplayName("스킬 거래 요청")
    class RequestSkillExchange {
    
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("스킬 거래 요청 시 거래가 생성(PENDING) 되고 멘티의 크레딧이 차감된다(거래 내역 포함).")
            public void success() {
                // given
                int duration = 60;
                int initialCredit = 10;
                User mentee = createSavedUser();
                // mentee 초기 크레딧 10개 설정
                createSavedCredit(mentee, initialCredit);

                User mentor = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), duration);
                createSavedUserProfile(mentor, mentorSkill);
                createSavedWeekDaysSchedule(mentor);

                // 월요일 날짜 및 10:00 시작 설정
                LocalDate targetDate = LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.MONDAY);
                LocalTime startTime = LocalTime.of(10, 0);

                String message = "defaultMessage";
                SkillExchangeDto exchangeDto = SkillExchangeDto.from(
                        new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(), message, targetDate, startTime));

                em.flush();
                em.clear();

                // when
                SkillExchangeResponseDto result = skillExchangeService.requestSkillExchange(mentee.getId(), exchangeDto);

                // then
                // return DTO 검증
                assertThat(result.getSkillExchangeId()).isNotNull();
                assertThat(result.getExchangeStatus()).isEqualTo(ExchangeStatus.PENDING.getDescription());

                // 스킬 거래 DB 저장 검증
                Integer exchangeDuration = mentorSkill.getExchangeDuration();
                assertThat(skillExchangeRepository.findById(result.getSkillExchangeId()))
                        .isPresent()
                        .get()
                        .satisfies(se -> {
                            assertThat(se.getRequester().getId()).isEqualTo(mentee.getId());
                            assertThat(se.getReceiver().getId()).isEqualTo(mentor.getId());
                            assertThat(se.getReceiverSkillId()).isEqualTo(mentorSkill.getId());
                            assertThat(se.getSkillName()).isEqualTo(mentorSkill.getSkillName());
                            assertThat(se.getExchangeDuration()).isEqualTo(exchangeDuration);
                            assertThat(se.getScheduledDate()).isEqualTo(targetDate);
                            assertThat(se.getStartTime()).isEqualTo(startTime);
                            assertThat(se.getEndTime()).isEqualTo(startTime.plusMinutes(exchangeDuration));
                            assertThat(se.getRequestDeadLine()).isEqualTo(targetDate.atStartOfDay());
                            assertThat(se.getCreditPrice()).isEqualTo(exchangeDuration / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES);
                            assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.PENDING);
                            assertThat(se.isRequesterRead()).isFalse();
                            assertThat(se.isReceiverRead()).isFalse();
                            assertThat(se.getMessage()).isEqualTo(message);
                        });

                // 크레딧 차감 검증 (2 크레딧 감소)
                Credit menteeCredit = creditRepository.findByUserId(mentee.getId()).orElseThrow();
                int expectedAmount = exchangeDuration / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;
                assertThat(menteeCredit.getBalance()).isEqualTo(initialCredit - expectedAmount);

                // 거래 내역 저장 확인
                List<CreditHistory> histories = creditHistoryRepository.findAllByUserId(mentee.getId());
                assertThat(histories).isNotEmpty();

                CreditHistory latestHistory = histories.get(histories.size() - 1);
                assertThat(latestHistory.getChangeAmount()).isEqualTo(expectedAmount);
                assertThat(latestHistory.getHistoryType()).isEqualTo(HistoryType.EXCHANGE_REQUEST);
                assertThat(latestHistory.getSkillExchange().getId()).isEqualTo(result.getSkillExchangeId());
            }
        }
    
        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("이미 동일한 시간에 다른 예약이 존재하면 AlreadyBookedExchangeTimeException 발생.")
            public void fail_AlreadyBookedExchangeTimeException_1() {
                // given
                User mentee1 = createSavedUser();
                User mentee2 = createSavedUser();
                createSavedCredit(mentee2, 10);

                User mentor = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), 60);
                createSavedUserProfile(mentor, mentorSkill);
                createSavedWeekDaysSchedule(mentor);

                LocalDate targetDate = LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.MONDAY);
                LocalTime startTime = LocalTime.of(10, 0);

                // 1. 첫 번째 멘티가 10:00 ~ 11:00 예약 완료
                createSavedExchange(mentee1, mentor, mentorSkill, targetDate, startTime, startTime.plusMinutes(60));

                // 2. 두 번째 멘티가 같은 시간에 요청하는 DTO 생성
                SkillExchangeDto exchangeDto = SkillExchangeDto.from(
                        new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(), "defaultMessage", targetDate, startTime));

                em.flush();
                em.clear();

                // when & then
                assertThatThrownBy(() -> skillExchangeService.requestSkillExchange(mentee2.getId(), exchangeDto))
                        .isInstanceOf(AlreadyBookedExchangeTimeException.class);
            }

            @Test
            @DisplayName("시간이 겹치는 다른 예약이 존재하면 AlreadyBookedExchangeTimeException 발생.")
            public void fail_AlreadyBookedExchangeTimeException_2() {
                // given
                int exchangeDuration = 60;
                User mentee1 = createSavedUser();
                User mentee2 = createSavedUser();
                createSavedCredit(mentee2, 10);

                User mentor = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), exchangeDuration);
                createSavedUserProfile(mentor, mentorSkill);
                createSavedWeekDaysSchedule(mentor);

                LocalDate targetDate = LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.MONDAY);
                LocalTime startTime = LocalTime.of(10, 0);

                // 1. 첫 번째 멘티가 10:00 ~ 11:00 예약 완료
                createSavedExchange(mentee1, mentor, mentorSkill, targetDate, startTime, startTime.plusMinutes(exchangeDuration));

                // 2. 두 번째 멘티가 10:30 ~ 11:30 요청하는 DTO 생성
                SkillExchangeDto exchangeDto = SkillExchangeDto.from(
                        new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(),
                                "defaultMessage", targetDate, startTime.plusMinutes(30)));

                em.flush();
                em.clear();

                // when & then
                assertThatThrownBy(() -> skillExchangeService.requestSkillExchange(mentee2.getId(), exchangeDto))
                        .isInstanceOf(AlreadyBookedExchangeTimeException.class);
            }

            @Test
            @DisplayName("자정을 넘어가는 예약 요청 시 OverExchangeDurationMidnightException 발생")
            public void fail_OverExchangeDurationMidnightException() {
                // given
                // 120분
                int exchangeDuration = 120;

                User mentee = createSavedUser();
                createSavedCredit(mentee, 10);

                User mentor = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), exchangeDuration);
                createSavedUserProfile(mentor, mentorSkill);

                LocalDate targetDate = LocalDate.now().plusDays(2);
                LocalTime startTime = LocalTime.of(23, 0);

                // 밤 23:00에 시작 -> 오전 01:00 종료 이기에 예외 발생
                SkillExchangeDto exchangeDto = SkillExchangeDto.from(
                        new SkillExchangeRequestDto(mentor.getId(), mentorSkill.getId(), "defaultMessage", targetDate, startTime));

                // when & then
                assertThatThrownBy(() -> skillExchangeService.requestSkillExchange(mentee.getId(), exchangeDto))
                        .isInstanceOf(OverExchangeDurationMidnightException.class);
            }
        }
    }

    @Transactional
    @Nested
    @DisplayName("스킬 거래 수락")
    class AcceptSkillExchange {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("멘토가 거래를 수락하면 거래의 상태가 ACCEPTED로 변경되고 정산 정보가 생성된다.")
            public void success() {
                // given
                User mentee = createSavedUser();
                User mentor = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), 60);
                createSavedUserProfile(mentor, mentorSkill);

                // 스킬 거래 요청 PENDING인 스킬 거래
                SkillExchange exchange = createSavedExchange(mentee, mentor, mentorSkill,
                        LocalDate.now().plusDays(3), LocalTime.of(14, 0), LocalTime.of(15, 0));

                Long exchangeId = exchange.getId();
                Long mentorId = mentor.getId();

                em.flush();
                em.clear();

                // when
                SkillExchangeResponseDto result = skillExchangeService.acceptSkillExchange(mentorId, exchangeId);

                // then
                // return DTO 검증
                assertThat(result.getSkillExchangeId()).isEqualTo(exchangeId);
                assertThat(result.getExchangeStatus()).isEqualTo(ExchangeStatus.ACCEPTED.getDescription());

                // DB 검증
                assertThat(skillExchangeRepository.findById(exchangeId))
                        .isPresent()
                        .get()
                                .satisfies(se -> {
                                    // 상태 변경 검증
                                    assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.ACCEPTED);
                                    // 요청자의 읽음 상태 검증 (요청자에게 알림을 위해 false 처리)
                                    assertThat(se.isRequesterRead()).isFalse();
                                });

                // 정산(settlement) 정보 생성 검증 (생성 여부 확인만)
                assertThat(settlementRepository.findBySkillExchangeId(exchangeId))
                        .isPresent()
                        .get()
                        .satisfies(settlement -> {
                           assertThat(settlement.getSkillExchange().getId()).isEqualTo(exchangeId);
                        });
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("존재하지 않는 거래로 수락 요청 시 ExchangeNotFoundException 발생")
            public void fail_ExchangeNotFound() {
                // given
                User mentor = createSavedUser();
                Long nonExistExchangeId = Long.MAX_VALUE;

                // when & then
                assertThatThrownBy(() -> skillExchangeService.acceptSkillExchange(mentor.getId(), nonExistExchangeId))
                        .isInstanceOf(ExchangeNotFoundException.class);
            }
            @Test
            @DisplayName("수신자가 아닌 다른 유저의 수락 요청 시 ExchangeAccessDeniedException 발생")
            public void fail_ExchangeAccessDeniedException() {
                // given
                User mentee = createSavedUser();
                User mentor = createSavedUser();
                User wrongUser = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), 60);
                createSavedUserProfile(mentor, mentorSkill);

                SkillExchange exchange = createSavedExchange(mentee, mentor, mentorSkill,
                        LocalDate.now().plusDays(3), LocalTime.of(14, 0), LocalTime.of(15, 0));

                // 요청자 읽음 표시
                exchange.updateRequesterReadToTrue();

                // when & then
                assertThatThrownBy(() -> skillExchangeService.acceptSkillExchange(wrongUser.getId(), exchange.getId()))
                        .isInstanceOf(ExchangeAccessDeniedException.class);
            }

            @Test
            @DisplayName("이미 거절되거나 취소된 거래를 수락하려 하면 InvalidExchangeStatusException 발생")
            public void fail_InvalidExchangeStatusException() {
                // given
                User mentee = createSavedUser();
                User mentor = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), 60);
                createSavedUserProfile(mentor, mentorSkill);

                SkillExchange exchange = createSavedExchange(mentee, mentor, mentorSkill,
                        LocalDate.now().plusDays(3), LocalTime.of(14, 0), LocalTime.of(15, 0));

                // REJECTED로 변경
                exchange.reject();
                em.flush();
                em.clear();

                // when & then
                assertThatThrownBy(() -> skillExchangeService.acceptSkillExchange(mentor.getId(), exchange.getId()))
                        .isInstanceOf(InvalidExchangeStatusException.class);
            }
        }
    }

    @Transactional
    @Nested
    @DisplayName("스킬 거래 거절")
    class RejectSkillExchange {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("멘토가 거래 거절 시 거래 상태가 REJECTED로 변경되고 멘티에게 크레딧이 환불된다.")
            public void success_rejectSkillExchange() {
                // given
                int exchangeDuration = 60;
                int creditPrice = exchangeDuration / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;
                int initialCredit = 10;
                User mentee = createSavedUser();
                // 스킬 거래 요청으로인한 크레딧 감소 처리
                createSavedCredit(mentee, initialCredit - creditPrice);

                User mentor = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), exchangeDuration);
                createSavedUserProfile(mentor, mentorSkill);

                // PENDING 상태의 거래 생성
                SkillExchange exchange = createSavedExchange(mentee, mentor, mentorSkill,
                        LocalDate.now().plusDays(3), LocalTime.of(14, 0), LocalTime.of(15, 0));

                // 요청자 읽음 표시
                exchange.updateRequesterReadToTrue();

                Long exchangeId = exchange.getId();
                Long mentorId = mentor.getId();

                em.flush();
                em.clear();

                // when
                SkillExchangeResponseDto response = skillExchangeService.rejectSkillExchange(mentorId, exchangeId);

                // then
                // return DTO 검증
                assertThat(response.getSkillExchangeId()).isEqualTo(exchangeId);
                assertThat(response.getExchangeStatus()).isEqualTo(ExchangeStatus.REJECTED.getDescription());

                // DB 상태 및 환불 검증
                assertThat(skillExchangeRepository.findById(exchangeId))
                        .isPresent()
                        .get()
                        .satisfies(se -> {
                            // 상태 변경 검증
                            assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.REJECTED);
                            // 요청자의 읽음 상태 검증 (요청자에게 알림을 위해 false 처리)
                            assertThat(se.isRequesterRead()).isFalse();
                        });

                // 멘티 크레딧 복구 확인 (8 -> 10)
                assertThat(creditRepository.findByUserId(mentee.getId()))
                        .isPresent()
                        .get()
                        .satisfies(credit -> {
                            assertThat(credit.getBalance()).isEqualTo(initialCredit);
                        });

                // 거래 내역 저장 확인
                List<CreditHistory> histories = creditHistoryRepository.findAllByUserId(mentee.getId());
                assertThat(histories).isNotEmpty();

                CreditHistory latestHistory = histories.get(histories.size() - 1);
                assertThat(latestHistory.getChangeAmount()).isEqualTo(creditPrice);
                assertThat(latestHistory.getHistoryType()).isEqualTo(HistoryType.EXCHANGE_REJECTED);
                assertThat(latestHistory.getSkillExchange().getId()).isEqualTo(exchangeId);
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("존재하지 않는 거래 거절 요청 시 ExchangeNotFoundException 발생")
            public void fail_ExchangeNotFoundException() {
                // given
                User mentor = createSavedUser();
                Long nonExistExchangeId = Long.MAX_VALUE;

                // when & then
                assertThatThrownBy(() -> skillExchangeService.rejectSkillExchange(mentor.getId(), nonExistExchangeId))
                        .isInstanceOf(ExchangeNotFoundException.class);
            }

            @Test
            @DisplayName("거래의 수신자(멘토)가 아닌 유저가 거절을 요청하면 ExchangeAccessDeniedException 발생")
            public void fail_ExchangeAccessDeniedException() {
                // given
                User mentee = createSavedUser();
                User mentor = createSavedUser();
                User otherUser = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), 60);
                createSavedUserProfile(mentor, mentorSkill);

                SkillExchange exchange = createSavedExchange(mentee, mentor, mentorSkill,
                        LocalDate.now().plusDays(3), LocalTime.of(14, 0), LocalTime.of(15, 0));

                // when & then
                assertThatThrownBy(() -> skillExchangeService.rejectSkillExchange(otherUser.getId(), exchange.getId()))
                        .isInstanceOf(ExchangeAccessDeniedException.class);
            }

            @Test
            @DisplayName("이미 수락되었거나 취소된 거래를 거절하려 하면 InvalidExchangeStatusException 발생")
            public void fail_InvalidExchangeStatusException() {
                // given
                User mentee = createSavedUser();
                User mentor = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), 60);
                createSavedUserProfile(mentor, mentorSkill);

                SkillExchange exchange = createSavedExchange(mentee, mentor, mentorSkill,
                        LocalDate.now().plusDays(3), LocalTime.of(14, 0), LocalTime.of(15, 0));

                // 이미 수락된 상태로 변경
                exchange.accept();
                em.flush();
                em.clear();

                // when & then
                assertThatThrownBy(() -> skillExchangeService.rejectSkillExchange(mentor.getId(), exchange.getId()))
                        .isInstanceOf(InvalidExchangeStatusException.class);
            }
        }

    }

    @Transactional
    @Nested
    @DisplayName("스킬 거래 취소")
    class CancelSkillExchange {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("멘티가 대기 중(PENDING)인 요청을 취소하면, 상태가 CANCELED로 변경되고 크레딧이 환불된다.")
            public void success_mentee_cancel_pending() {
                // given
                int exchangeDuration = 60;
                int initialCredit = 8;
                int creditPrice = exchangeDuration / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;
                User mentee = createSavedUser();
                createSavedCredit(mentee, initialCredit);

                User mentor = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), exchangeDuration);
                createSavedUserProfile(mentor, mentorSkill);

                // PENDING 상태 거래 생성 (3일 뒤)
                SkillExchange exchange = createSavedExchange(mentee, mentor, mentorSkill,
                        LocalDate.now().plusDays(3), LocalTime.of(14, 0), LocalTime.of(15, 0));
                Long exchangeId = exchange.getId();

                // receiver 읽음 처리
                exchange.updateReceiverReadToTrue();

                em.flush();
                em.clear();

                // when
                SkillExchangeResponseDto result = skillExchangeService.cancelSkillExchange(mentee.getId(), exchange.getId());

                // then
                // return DTO 검증
                assertThat(result.getSkillExchangeId()).isEqualTo(exchangeId);
                assertThat(result.getExchangeStatus()).isEqualTo(ExchangeStatus.CANCELED.getDescription());

                //
                // DB 상태 및 환불 검증
                assertThat(skillExchangeRepository.findById(exchangeId))
                        .isPresent()
                        .get()
                        .satisfies(se -> {
                            // 상태 변경 검증
                            assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.CANCELED);
                            // 수신자의 읽음 상태 검증 (요청자에게 알림을 위해 false 처리)
                            assertThat(se.isReceiverRead()).isFalse();
                        });

                // 멘티 크레딧 복구 확인 (8 -> 10)
                assertThat(creditRepository.findByUserId(mentee.getId()))
                        .isPresent()
                        .get()
                        .satisfies(credit -> {
                            assertThat(credit.getBalance()).isEqualTo(initialCredit + creditPrice);
                        });

                // 거래 내역 저장 확인
                List<CreditHistory> histories = creditHistoryRepository.findAllByUserId(mentee.getId());
                assertThat(histories).isNotEmpty();

                CreditHistory latestHistory = histories.get(histories.size() - 1);
                assertThat(latestHistory.getChangeAmount()).isEqualTo(creditPrice);
                assertThat(latestHistory.getHistoryType()).isEqualTo(HistoryType.EXCHANGE_CANCELED);
                assertThat(latestHistory.getSkillExchange().getId()).isEqualTo(exchangeId);
            }

            @Test
            @DisplayName("멘티가 수락된(ACCEPTED)인 요청을 취소하면, 상태가 CANCELED로 변경되고 크레딧이 환불된다.")
            public void success_mentee_cancel_accepted() {
                // given
                int exchangeDuration = 60;
                int initialCredit = 8;
                int creditPrice = exchangeDuration / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;
                User mentee = createSavedUser();
                createSavedCredit(mentee, initialCredit);

                User mentor = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), exchangeDuration);
                createSavedUserProfile(mentor, mentorSkill);

                // PENDING 상태 거래 생성 (3일 뒤)
                SkillExchange exchange = createSavedExchange(mentee, mentor, mentorSkill,
                        LocalDate.now().plusDays(3), LocalTime.of(14, 0), LocalTime.of(15, 0));
                Long exchangeId = exchange.getId();

                createSavedSettlement(exchange, mentor);

                // 스킬 거래 ACCEPTED 처리
                exchange.updateExchangeStatus(ExchangeStatus.ACCEPTED);

                // receiver 읽음 처리
                exchange.updateReceiverReadToTrue();

                em.flush();
                em.clear();

                // when
                SkillExchangeResponseDto result = skillExchangeService.cancelSkillExchange(mentee.getId(), exchange.getId());

                // then
                // return DTO 검증
                assertThat(result.getSkillExchangeId()).isEqualTo(exchangeId);
                assertThat(result.getExchangeStatus()).isEqualTo(ExchangeStatus.CANCELED.getDescription());

                // DB 상태 및 환불 검증
                assertThat(skillExchangeRepository.findById(exchangeId))
                        .isPresent()
                        .get()
                        .satisfies(se -> {
                            // 상태 변경 검증
                            assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.CANCELED);
                            // 수신자의 읽음 상태 검증 (요청자에게 알림을 위해 false 처리)
                            assertThat(se.isReceiverRead()).isFalse();
                        });

                // 멘티 크레딧 복구 확인 (8 -> 10)
                assertThat(creditRepository.findByUserId(mentee.getId()))
                        .isPresent()
                        .get()
                        .satisfies(credit -> {
                            assertThat(credit.getBalance()).isEqualTo(initialCredit + creditPrice);
                        });

                // 거래 내역 저장 확인
                List<CreditHistory> histories = creditHistoryRepository.findAllByUserId(mentee.getId());
                assertThat(histories).isNotEmpty();

                CreditHistory latestHistory = histories.get(histories.size() - 1);
                assertThat(latestHistory.getChangeAmount()).isEqualTo(creditPrice);
                assertThat(latestHistory.getHistoryType()).isEqualTo(HistoryType.EXCHANGE_CANCELED);
                assertThat(latestHistory.getSkillExchange().getId()).isEqualTo(exchangeId);

                // 정산 정보 상태 검증
                assertThat(settlementRepository.findBySkillExchangeId(exchangeId))
                        .isPresent()
                        .get()
                        .satisfies(settlement -> {
                            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CANCELED);
                        });
            }

            @Test
            @DisplayName("멘토가 수락된(ACCEPTED) 거래를 취소하면, 상태가 CANCELED로 변경되고 정산이 취소되며 멘티에게 환불된다.")
            public void success_mentor_cancel_accepted() {
                // given
                int exchangeDuration = 60;
                int initialCredit = 8;
                int creditPrice = exchangeDuration / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;
                User mentee = createSavedUser();
                createSavedCredit(mentee, initialCredit);

                User mentor = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), exchangeDuration);
                createSavedUserProfile(mentor, mentorSkill);

                // 거래 요청 및 수락 처리
                SkillExchange exchange = createSavedExchange(mentee, mentor, mentorSkill,
                        LocalDate.now().plusDays(3), LocalTime.of(14, 0), LocalTime.of(15, 0));
                skillExchangeService.acceptSkillExchange(mentor.getId(), exchange.getId());

                Long exchangeId = exchange.getId();

                // 요청자 읽음 처리
                exchange.updateRequesterReadToTrue();

                em.flush();
                em.clear();

                // when
                SkillExchangeResponseDto result = skillExchangeService.cancelSkillExchange(mentor.getId(), exchange.getId());

                // then
                // return DTO 검증
                assertThat(result.getSkillExchangeId()).isEqualTo(exchangeId);
                assertThat(result.getExchangeStatus()).isEqualTo(ExchangeStatus.CANCELED.getDescription());

                // DB 상태 및 환불 검증
                assertThat(skillExchangeRepository.findById(exchangeId))
                        .isPresent()
                        .get()
                        .satisfies(se -> {
                            // 상태 변경 검증
                            assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.CANCELED);
                            // 요청자의 읽음 상태 검증
                            assertThat(se.isRequesterRead()).isFalse();
                        });

                // 멘티 크레딧 복구 확인 (8 -> 10)
                assertThat(creditRepository.findByUserId(mentee.getId()))
                        .isPresent()
                        .get()
                        .satisfies(credit -> {
                            assertThat(credit.getBalance()).isEqualTo(initialCredit + creditPrice);
                        });

                // 거래 내역 저장 확인
                List<CreditHistory> histories = creditHistoryRepository.findAllByUserId(mentee.getId());
                assertThat(histories).isNotEmpty();

                CreditHistory latestHistory = histories.get(histories.size() - 1);
                assertThat(latestHistory.getChangeAmount()).isEqualTo(creditPrice);
                assertThat(latestHistory.getHistoryType()).isEqualTo(HistoryType.EXCHANGE_CANCELED);
                assertThat(latestHistory.getSkillExchange().getId()).isEqualTo(exchangeId);

                // 정산 정보 상태 검증
                assertThat(settlementRepository.findBySkillExchangeId(exchangeId))
                        .isPresent()
                        .get()
                        .satisfies(settlement -> {
                            assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CANCELED);
                        });
            }
        }

        @Nested
        @DisplayName("실패 케이스")
        class FailCases {

            @Test
            @DisplayName("존재하지 않는 거래로 취소 시도 시 ExchangeNotFoundException 발생")
            public void fail_ExchangeNotFoundException() {
                // given
                User user = createSavedUser();
                Long nonExistExchangeId = Long.MAX_VALUE;

                // when & then
                assertThatThrownBy(() -> skillExchangeService.cancelSkillExchange(user.getId(), nonExistExchangeId))
                        .isInstanceOf(ExchangeNotFoundException.class);
            }

            @Test
            @DisplayName("거래 당사자(요청자 또는 수신자)가 아닌 유저가 취소를 시도하면 ExchangeAccessDeniedException 발생")
            public void fail_ExchangeAccessDeniedException() {
                // given
                User mentee = createSavedUser();
                User mentor = createSavedUser();
                User otherUsser = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), 60);
                createSavedUserProfile(mentor, mentorSkill);

                SkillExchange exchange = createSavedExchange(mentee, mentor, mentorSkill,
                        LocalDate.now().plusDays(3), LocalTime.of(14, 0), LocalTime.of(15, 0));

                // when & then
                assertThatThrownBy(() -> skillExchangeService.cancelSkillExchange(otherUsser.getId(), exchange.getId()))
                        .isInstanceOf(ExchangeAccessDeniedException.class);
            }

            @Test
            @DisplayName("멘티가 대기중, 수락된 요청이 아닌 상태의 거래 취소를 시도하면 InvalidExchangeStatusException 발생")
            public void fail_mentee_InvalidExchangeStatusException() {
                // given
                User mentee = createSavedUser();
                User mentor = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), 60);
                createSavedUserProfile(mentor, mentorSkill);
                SkillExchange exchange = createSavedExchange(mentee, mentor, mentorSkill,
                        LocalDate.now().plusDays(3), LocalTime.of(14, 0), LocalTime.of(15, 0));

                // 거래 완료(COMPLTED) 처리
                exchange.updateExchangeStatus(ExchangeStatus.COMPLETED);

                em.flush();
                em.clear();

                // when & then
                assertThatThrownBy(() -> skillExchangeService.cancelSkillExchange(mentee.getId(), exchange.getId()))
                        .isInstanceOf(InvalidExchangeStatusException.class);
            }

            @Test
            @DisplayName("멘토가 수락된 요청이 아닌 상태의 거래 취소를 시도하면 InvalidExchangeStatusException 발생")
            public void fail_mentor_InvalidExchangeStatusException() {
                // given
                User mentee = createSavedUser();
                createSavedCredit(mentee, 10);
                User mentor = createSavedUser();
                UserSkill mentorSkill = createUserSkill(createSavedSkillCategory(), 60);
                createSavedUserProfile(mentor, mentorSkill);
                SkillExchange exchange = createSavedExchange(mentee, mentor, mentorSkill,
                        LocalDate.now().plusDays(3), LocalTime.of(14, 0), LocalTime.of(15, 0));

                em.flush();
                em.clear();

                // when & then
                assertThatThrownBy(() -> skillExchangeService.cancelSkillExchange(mentor.getId(), exchange.getId()))
                        .isInstanceOf(InvalidExchangeStatusException.class);
            }
        }
    }

    @Transactional
    @Nested
    @DisplayName("스킬 거래 보낸 요청 목록 조회")
    class GetSentRequests {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("DTO Projection으로 조회 후 응답 DTO에 성공적으로 매핑 되어야한다.")
            public void success_DtoMapping() {
                // given
                // 멘토 1명에게 거래 요청 시나리오
                int exchangeDuration = 60;
                User requester = createSavedUser();
                User mentor = createSavedUser();
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill mentorSkill = createUserSkill(skillCategory, exchangeDuration);
                createSavedUserProfile(mentor, mentorSkill);
                SkillExchange skillExchange = createSavedExchange(requester, mentor, mentorSkill, LocalDate.now().minusDays(1),
                        LocalTime.now(), LocalTime.now().plusMinutes(exchangeDuration));
                ChatRoom chatRoom = createCharRoom(mentor, requester);

                Long requesterId = requester.getId();
                int size = 1;
                Long cursor = null;

                em.flush();
                em.clear();

                // when
                SkillExchangeDetailsResponseDto result = skillExchangeService.getSentRequests(requesterId, cursor, size);

                // then
                // return DTO 검증
                assertThat(result.getContents()).hasSize(size);
                assertThat(result.isHasNext()).isFalse();
                assertThat(result.getNextCursor()).isNull();

                // return DTO 매핑 검증
                List<SkillExchangeDetailDto> contents = result.getContents();
                SkillExchangeDetailDto dto = contents.get(0);
                validateSkillExchangeDetailDtoMapping(dto, skillExchange, mentor, mentorSkill, chatRoom);
            }

            @Test
            @DisplayName("첫 페이지 조회 시 다음 커서가 존재해야 한다.")
            public void getReceivedRequests_firstPage() {
                // given
                User requester = createSavedUser();

                // 멘토 5명에게 각각 1건의 요청 시나리오
                List<User> mentors = List.of(createSavedUser(), createSavedUser(), createSavedUser(), createSavedUser(), createSavedUser());
                // 요청 생성
                setupMultipleSentRequests(requester, mentors);

                Long requesterId = requester.getId();
                int size = 2;
                Long cursor = null;

                em.flush();
                em.clear();

                // when
                SkillExchangeDetailsResponseDto result = skillExchangeService.getSentRequests(requesterId, cursor, size);

                List<SkillExchangeDetailDto> contents = result.getContents();
                // then
                assertThat(contents).hasSize(size);
                // 5개의 요청 중 2개를 조회 했기에 다음 페이지 존재
                assertThat(result.isHasNext()).isTrue();
                // 다음 커서 검증
                assertThat(result.getNextCursor()).isEqualTo(contents.get(size - 1).getSkillExchangeId());

                // 정렬 검증
                Long firstId = contents.get(0).getSkillExchangeId();
                Long secondId = contents.get(1).getSkillExchangeId();
                assertThat(firstId).isGreaterThan(secondId);
            }

            @Test
            @DisplayName("커서가 존재할 때 커서 이후의 데이터를 조회해야한다.")
            public void getReceivedRequests_secondPage() {
                // given
                User requester = createSavedUser();

                // 멘토 5명에게 각각 1건의 요청 시나리오
                List<User> mentors = List.of(createSavedUser(), createSavedUser(), createSavedUser(), createSavedUser(), createSavedUser());
                // 요청 생성
                List<Long> skillExchangeIds = setupMultipleSentRequests(requester, mentors);

                Long requesterId = requester.getId();
                int size = 2;
                Long cursor = skillExchangeIds.get(skillExchangeIds.size() - size);

                em.flush();
                em.clear();

                // when
                SkillExchangeDetailsResponseDto result = skillExchangeService.getSentRequests(requesterId, cursor, size);

                List<SkillExchangeDetailDto> contents = result.getContents();
                // then
                assertThat(contents).hasSize(size);
                // 5개의 요청 중 2번 부터 4번 데이터를 조회 했기에 다음 페이지 존재
                assertThat(result.isHasNext()).isTrue();
                // 다음 커서 검증
                assertThat(result.getNextCursor()).isEqualTo(contents.get(size - 1).getSkillExchangeId());

                // 정렬 검증
                Long firstId = contents.get(0).getSkillExchangeId();
                Long secondId = contents.get(1).getSkillExchangeId();
                assertThat(firstId).isGreaterThan(secondId);
            }
        }
    }

    @Transactional
    @Nested
    @DisplayName("스킬 거래 받은 요청 목록 조회")
    class GetReceivedRequests {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("DTO Projection으로 조회 후 응답 DTO에 성공적으로 매핑 되어야한다.")
            public void success_DtoMapping() {
                // given
                // 멘티 1명에게 거래 요청 받은 시나리오
                int exchangeDuration = 60;
                User requester = createSavedUser();
                User mentor = createSavedUser();
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill mentorSkill = createUserSkill(skillCategory, exchangeDuration);
                createSavedUserProfile(mentor, mentorSkill);
                SkillExchange skillExchange = createSavedExchange(requester, mentor, mentorSkill, LocalDate.now().minusDays(1),
                        LocalTime.now(), LocalTime.now().plusMinutes(exchangeDuration));
                ChatRoom chatRoom = createCharRoom(mentor, requester);

                Long receiverId = mentor.getId();
                int size = 1;
                Long cursor = null;

                em.flush();
                em.clear();

                // when
                SkillExchangeDetailsResponseDto result = skillExchangeService.getReceivedRequests(receiverId, cursor, size);

                // then
                // return DTO 검증
                assertThat(result.getContents()).hasSize(size);
                assertThat(result.isHasNext()).isFalse();
                assertThat(result.getNextCursor()).isNull();

                // return DTO 매핑 검증
                List<SkillExchangeDetailDto> contents = result.getContents();
                SkillExchangeDetailDto dto = contents.get(0);
                validateSkillExchangeDetailDtoMapping(dto, skillExchange, requester, mentorSkill, chatRoom);
            }

            @Test
            @DisplayName("첫 페이지 조회 시 다음 커서가 존재해야 한다.")
            public void getSentRequests_firstPage() {
                // given
                User mentor = createSavedUser();

                // 멘티 5명에게 요청 받은 시나리오
                List<User> requestors = List.of(createSavedUser(), createSavedUser(), createSavedUser(), createSavedUser(), createSavedUser());
                // 요청 생성
                setupMultipleReceivedRequests(requestors, mentor);

                Long receiverId = mentor.getId();
                int size = 2;
                Long cursor = null;

                em.flush();
                em.clear();

                // when
                SkillExchangeDetailsResponseDto result = skillExchangeService.getReceivedRequests(receiverId, cursor, size);

                List<SkillExchangeDetailDto> contents = result.getContents();
                // then
                assertThat(contents).hasSize(size);
                // 5개의 요청 중 2개를 조회 했기에 다음 페이지 존재
                assertThat(result.isHasNext()).isTrue();
                // 다음 커서 검증
                assertThat(result.getNextCursor()).isEqualTo(contents.get(size - 1).getSkillExchangeId());

                // 정렬 검증
                Long firstId = contents.get(0).getSkillExchangeId();
                Long secondId = contents.get(1).getSkillExchangeId();
                assertThat(firstId).isGreaterThan(secondId);
            }

            @Test
            @DisplayName("커서가 존재할 때 커서 이후의 데이터를 조회해야한다.")
            public void getSentRequests_secondPage() {
                // given
                User mentor = createSavedUser();

                // 멘티 5명에게 요청 받은 시나리오
                List<User> requestors = List.of(createSavedUser(), createSavedUser(), createSavedUser(), createSavedUser(), createSavedUser());
                // 요청 생성
                List<Long> skillExchangeIds = setupMultipleReceivedRequests(requestors, mentor);

                Long receiverId = mentor.getId();
                int size = 2;
                Long cursor = skillExchangeIds.get(skillExchangeIds.size() - size);

                em.flush();
                em.clear();

                // when
                SkillExchangeDetailsResponseDto result = skillExchangeService.getReceivedRequests(receiverId, cursor, size);

                List<SkillExchangeDetailDto> contents = result.getContents();
                // then
                assertThat(contents).hasSize(size);
                // 5개의 요청 중 2번 부터 4번 데이터를 조회 했기에 다음 페이지 존재
                assertThat(result.isHasNext()).isTrue();
                // 다음 커서 검증
                assertThat(result.getNextCursor()).isEqualTo(contents.get(size - 1).getSkillExchangeId());

                // 정렬 검증
                Long firstId = contents.get(0).getSkillExchangeId();
                Long secondId = contents.get(1).getSkillExchangeId();
                assertThat(firstId).isGreaterThan(secondId);
            }
        }
    }

    @Transactional
    @Nested
    @DisplayName("유저의 요청 관리에 읽지 않은 알림이 있는지 조회.")
    class GetNotification {

        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("유저의 요청 관리에 읽지 않은 신규 알림이 존재하면 true로 표시된다.")
            public void success() {
                // given
                User user1 = createSavedUser();
                User user2 = createSavedUser();

                int exchangeDuration = 60;
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill user1Skill = createUserSkill(skillCategory, exchangeDuration);
                createSavedUserProfile(user1, user1Skill);
                UserSkill user2Skill = createUserSkill(skillCategory, exchangeDuration);
                createSavedUserProfile(user2, user2Skill);

                // 받은 요청 2개, 보낸 요청 2개 시나리오
                LocalDate now = LocalDate.now();
                LocalTime start = LocalTime.now();
                SkillExchange exchange1 = createSavedExchange(user1, user2, user2Skill, now, start, start.plusMinutes(exchangeDuration));
                SkillExchange exchange2 = createSavedExchange(user1, user2, user2Skill, now, start, start.plusMinutes(exchangeDuration));
                exchange1.updateRequesterReadToTrue();
                exchange2.updateRequesterReadToTrue();

                SkillExchange exchange3 = createSavedExchange(user2, user1, user1Skill, now, start, start.plusMinutes(exchangeDuration));
                exchange1.updateRequesterReadToTrue();
                SkillExchange exchange4 = createSavedExchange(user2, user1, user1Skill, now, start, start.plusMinutes(exchangeDuration));

                // when
                SkillExchangeNotificationResponseDto result = skillExchangeService.getNotification(user1.getId());

                // then
                // 보낸 요청은 모두 확인했기에 false
                // 받은 요청은 하나만 확인했기에 true
                assertThat(result.isHasAnyUnread()).isEqualTo(true);
                assertThat(result.isHasUnreadSent()).isEqualTo(false);
                assertThat(result.isHasUnreadReceived()).isEqualTo(true);
            }
        }
    }

    // todo
    @Nested
    @DisplayName("거래 날짜 전날까지 수락되지 않은 요청 만료(거절).")
    class ExpirePendingRequests {
    
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            @Test
            @DisplayName("만료 기한이 지난 PENDING 상태의 요청들을 만료 처리하고 크레딧을 환불한다")
            public void success() {
                // given
                LocalDate today = LocalDate.now();
                LocalDate yesterday = today.minusDays(1);
                LocalDate tomorrow = today.plusDays(1);

                int exchangeDuration = 60;
                int creditPrice = exchangeDuration / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;
                User requester = createSavedUser();
                int requesterBalance = createSavedCredit(requester, 100).getBalance();

                User receiver = createSavedUser();
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill mentorSkill = createUserSkill(skillCategory, exchangeDuration);
                createSavedUserProfile(receiver, mentorSkill);

                // 어제 거래 및 PENDING -> 만료 대상
                SkillExchange targetExchange = createSavedExchange(requester, receiver, mentorSkill, yesterday,
                        LocalTime.now(), LocalTime.now().plusMinutes(exchangeDuration));
                targetExchange.updateReceiverReadToTrue();
                targetExchange.updateRequesterReadToTrue();
                skillExchangeRepository.saveAndFlush(targetExchange);

                // 내일 거래 및 PENDING -> 만료 대상 아님
                SkillExchange noneTargetExchange = createSavedExchange(requester, receiver, mentorSkill, tomorrow,
                        LocalTime.now(), LocalTime.now().plusMinutes(exchangeDuration));
                noneTargetExchange.updateReceiverReadToTrue();
                noneTargetExchange.updateRequesterReadToTrue();
                skillExchangeRepository.saveAndFlush(noneTargetExchange);

                // when
                System.out.println("=== 로직 시작 ==");
                int successCount = skillExchangeService.expirePendingRequests();

                // then
                // 만료 처리된 데이터는 1건
                assertThat(successCount).isEqualTo(1);

                // 트랜잭션 종료로 인해 lazyLoading 사용 불가능
                // DB 검증
                // 1. 대상 건 검증
                // skillExchange 상태 및 requester, receiver 미확인 처리 검증
                assertThat(skillExchangeRepository.findById(targetExchange.getId()))
                        .isPresent()
                        .get()
                        .satisfies(se -> {
                            assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.EXPIRED);
                            assertThat(se.isRequesterRead()).isFalse();
                            assertThat(se.isReceiverRead()).isFalse();
                        });

                // credit 환불 검증
                assertThat(creditRepository.findByUserId(requester.getId()))
                        .isPresent()
                        .get()
                        .satisfies(credit -> {
                            assertThat(credit.getBalance()).isEqualTo(requesterBalance + creditPrice);
                        });

                // 2. 대상 아닌 건 검증
                // skillExchange 상태 및 requester, receiver 확인 처리 검증
                assertThat(skillExchangeRepository.findById(noneTargetExchange.getId()))
                        .isPresent()
                        .get()
                        .satisfies(se -> {
                            assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.PENDING);
                            assertThat(se.isRequesterRead()).isTrue();
                            assertThat(se.isReceiverRead()).isTrue();
                        });

                cleanUp();
            }
        }
    
        @Nested
        @DisplayName("실패 케이스")
        class FailCases {
            @Test
            @DisplayName("여러 건 중 하나가 실패하더라도 REQUIRES_NEW에 의해 다른 건들은 정상 처리되어야 한다.")
            public void success_IndependentTransaction() {
                // given
                LocalDate today = LocalDate.now();
                LocalDate yesterday = today.minusDays(1);

                int exchangeDuration = 60;
                int creditPrice = exchangeDuration / SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;
                User targetRequester = createSavedUser();
                int requesterBalance = createSavedCredit(targetRequester, 100).getBalance();

                User noneTargetRequester = createSavedUser();

                User receiver = createSavedUser();
                SkillCategory skillCategory = createSavedSkillCategory();
                UserSkill mentorSkill = createUserSkill(skillCategory, exchangeDuration);
                createSavedUserProfile(receiver, mentorSkill);

                // 어제 거래 및 PENDING -> 만료 대상
                SkillExchange targetExchange = createSavedExchange(targetRequester, receiver, mentorSkill, yesterday,
                        LocalTime.now(), LocalTime.now().plusMinutes(exchangeDuration));
                targetExchange.updateReceiverReadToTrue();
                targetExchange.updateRequesterReadToTrue();
                skillExchangeRepository.saveAndFlush(targetExchange);

                // 어제 거래 및 PENDING -> 만료 대상. 하지만 requester의 credit 정보가 존재하지 않아 예외 발생
                SkillExchange noneTargetExchange = createSavedExchange(noneTargetRequester, receiver, mentorSkill, yesterday,
                        LocalTime.now(), LocalTime.now().plusMinutes(exchangeDuration));
                noneTargetExchange.updateReceiverReadToTrue();
                noneTargetExchange.updateRequesterReadToTrue();
                skillExchangeRepository.saveAndFlush(noneTargetExchange);

                // when
                System.out.println("=== 로직 시작 ==");
                int successCount = skillExchangeService.expirePendingRequests();

                // then
                // 만료 처리된 데이터는 1건
                assertThat(successCount).isEqualTo(1);

                // 트랜잭션 종료로 인해 lazyLoading 사용 불가능
                // DB 검증
                // 1. 대상 건 검증
                // skillExchange 상태 및 requester, receiver 미확인 처리 검증
                assertThat(skillExchangeRepository.findById(targetExchange.getId()))
                        .isPresent()
                        .get()
                        .satisfies(se -> {
                            assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.EXPIRED);
                            assertThat(se.isRequesterRead()).isFalse();
                            assertThat(se.isReceiverRead()).isFalse();
                        });

                // credit 환불 검증
                assertThat(creditRepository.findByUserId(targetRequester.getId()))
                        .isPresent()
                        .get()
                        .satisfies(credit -> {
                            assertThat(credit.getBalance()).isEqualTo(requesterBalance + creditPrice);
                        });

                // 2. 대상 아닌 건 검증
                // skillExchange 상태 및 requester, receiver 확인 처리 검증
                assertThat(skillExchangeRepository.findById(noneTargetExchange.getId()))
                        .isPresent()
                        .get()
                        .satisfies(se -> {
                            assertThat(se.getExchangeStatus()).isEqualTo(ExchangeStatus.PENDING);
                            assertThat(se.isRequesterRead()).isTrue();
                            assertThat(se.isReceiverRead()).isTrue();
                        });

                cleanUp();
            }
        }
    }

    /***
     *  월 ~ 금 일정 생성
     *  월 ~ 수 -> [10:00 ~ 12:00],[18:00 ~ 20:00]
     *  목 ~ 금 -> [11:30 ~ 13:30],[22:00 ~ 24:00]
     */
    private List<AvailableSchedule> createSavedWeekDaysSchedule(User user) {
        LocalTime monWedFirstStartTime = LocalTime.of(10, 0);
        LocalTime monWedFirstEndTime = LocalTime.of(12, 0);

        LocalTime monWedSecondStartTime = LocalTime.of(18, 0);
        LocalTime monWedSecondEndTime = LocalTime.of(20, 0);

        LocalTime thuFriFirstStartTime = LocalTime.of(11, 30);
        LocalTime thuFriFirstEndTime = LocalTime.of(13, 30);

        LocalTime thuFriSecondStartTime = LocalTime.of(22, 0);
        LocalTime thuFriSecondEndTime = LocalTime.of(0, 0);

        List<AvailableSchedule> availableSchedules = new ArrayList<>();

        // 월
        AvailableSchedule mon1 = AvailableSchedule.create(user, Weekday.MON, monWedFirstStartTime, monWedFirstEndTime);
        AvailableSchedule mon2 = AvailableSchedule.create(user, Weekday.MON, monWedSecondStartTime, monWedSecondEndTime);
        availableSchedules.add(availableScheduleRepository.save(mon1));
        availableSchedules.add(availableScheduleRepository.save(mon2));
        // 화
        AvailableSchedule tue1 = AvailableSchedule.create(user, Weekday.TUE, monWedFirstStartTime, monWedFirstEndTime);
        AvailableSchedule tue2 = AvailableSchedule.create(user, Weekday.TUE, monWedSecondStartTime, monWedSecondEndTime);
        availableSchedules.add(availableScheduleRepository.save(tue1));
        availableSchedules.add(availableScheduleRepository.save(tue2));
        // 수
        AvailableSchedule wed1 = AvailableSchedule.create(user, Weekday.WED, monWedFirstStartTime, monWedFirstEndTime);
        AvailableSchedule wed2 = AvailableSchedule.create(user, Weekday.WED, monWedSecondStartTime, monWedSecondEndTime);
        availableSchedules.add(availableScheduleRepository.save(wed1));
        availableSchedules.add(availableScheduleRepository.save(wed2));
        // 목
        AvailableSchedule thu1 = AvailableSchedule.create(user, Weekday.THU, thuFriFirstStartTime, thuFriFirstEndTime);
        AvailableSchedule thu2 = AvailableSchedule.create(user, Weekday.THU, thuFriSecondStartTime, thuFriSecondEndTime);
        availableSchedules.add(availableScheduleRepository.save(thu1));
        availableSchedules.add(availableScheduleRepository.save(thu2));
        // 금
        AvailableSchedule fri1 = AvailableSchedule.create(user, Weekday.FRI, thuFriFirstStartTime, thuFriFirstEndTime);
        AvailableSchedule fri2 = AvailableSchedule.create(user, Weekday.FRI, thuFriSecondStartTime, thuFriSecondEndTime);
        availableSchedules.add(availableScheduleRepository.save(fri1));
        availableSchedules.add(availableScheduleRepository.save(fri2));

        return availableSchedules;
    }

    private void cleanUp() {
        settlementRepository.deleteAllInBatch();
        creditHistoryRepository.deleteAllInBatch();
        chatRoomRepository.deleteAllInBatch();
        skillExchangeRepository.deleteAllInBatch();
        userSkillRepository.deleteAllInBatch();
        availableScheduleRepository.deleteAllInBatch();
        userProfileRepository.deleteAllInBatch();
        creditRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        skillCategoryRepository.deleteAllInBatch();
    }

    private void validateSkillExchangeDetailDtoMapping(SkillExchangeDetailDto dto, SkillExchange skillExchange, User targetUser, UserSkill mentorSkill, ChatRoom chatRoom) {
        assertThat(dto.getSkillExchangeId()).isEqualTo(skillExchange.getId());
        assertThat(dto.getTargetUserId()).isEqualTo(targetUser.getId());
        assertThat(dto.getSkillId()).isEqualTo(mentorSkill.getId());
        assertThat(dto.getChatRoomId()).isEqualTo(chatRoom.getId());
        assertThat(dto.getTargetProfileImageUrl()).isEqualTo(targetUser.getProfileImageUrl());
        assertThat(dto.getTargetNickname()).isEqualTo(targetUser.getNickname());
        assertThat(dto.getSkillName()).isEqualTo(mentorSkill.getSkillName());
        assertThat(dto.getExchangeStatus()).isEqualTo(skillExchange.getExchangeStatus().getDescription());
        assertThat(dto.getCreditPrice()).isEqualTo(skillExchange.getCreditPrice());
        assertThat(dto.getMessage()).isEqualTo(skillExchange.getMessage());
        assertThat(dto.getRequestedDate()).isEqualTo(skillExchange.getCreatedAt().toLocalDate());
        assertThat(dto.getExchangeDateTime()).isEqualTo(skillExchange.getScheduledDate()
                .atTime(skillExchange.getStartTime()).truncatedTo(ChronoUnit.SECONDS));
        assertThat(dto.getExchangeDuration()).isEqualTo(skillExchange.getExchangeDuration());
        assertThat(dto.isNew()).isEqualTo(!skillExchange.isRequesterRead());
    }

    private List<Long> setupMultipleReceivedRequests(List<User> requesters, User mentor) {
        List<Long> ids = new ArrayList<>();
        SkillCategory category = createSavedSkillCategory();
        UserSkill skill = createUserSkill(category, 60);
        createSavedUserProfile(mentor, skill);

        for(int i = 0; i < requesters.size(); i++) {
            User requester = requesters.get(i);

            LocalDate targetDate = LocalDate.now().plusDays(2 + i);
            LocalTime startTime = LocalTime.of(10, 0).plusMinutes(i * 10L);
            LocalTime endTime = startTime.plusMinutes(60);

            SkillExchange exchange = createSavedExchange(requester, mentor, skill, targetDate, startTime, endTime);
            ids.add(exchange.getId());
        }
        return ids;
    }


    private List<Long> setupMultipleSentRequests(User requester, List<User> mentors) {
        List<Long> ids = new ArrayList<>();
        SkillCategory category = createSavedSkillCategory();
        UserSkill skill = createUserSkill(category, 60);

        for(int i = 0; i < mentors.size(); i++) {
            User mentor = mentors.get(i);
            createSavedUserProfile(mentor, skill);

            LocalDate targetDate = LocalDate.now().plusDays(2 + i);
            LocalTime startTime = LocalTime.of(10, 0).plusMinutes(i * 10L);
            LocalTime endTime = startTime.plusMinutes(60);

            SkillExchange exchange = createSavedExchange(requester, mentor, skill, targetDate, startTime, endTime);
            ids.add(exchange.getId());
        }
        return ids;
    }

    private boolean findSlot(List<SlotDto> slots, int hour, int minute) {
        LocalTime targetTime = LocalTime.of(hour, minute);
        return slots.stream()
                .anyMatch(slot -> slot.getStartTime().equals(targetTime));
    }

    private ChatRoom createCharRoom(User mentor, User mentee){
        return chatRoomRepository.save(ChatRoom.create(mentor, mentee));
    }

    private Settlement createSavedSettlement(SkillExchange skillExchange, User receiver) {
        return settlementRepository.save(Settlement.create(skillExchange, receiver));
    }

    private UserProfile createSavedUserProfile(User user, UserSkill userSkill) {
        UserProfile userProfile = UserProfile.create(
                user,
                "description"
                , ExchangeType.OFFLINE,
                null,
                null
        );
        userProfile.addUserSkill(userSkill);
        userProfileRepository.save(userProfile);
        userSkillRepository.save(userSkill);
        return userProfile;
    }

    private SkillCategory createSavedSkillCategory() {
        return skillCategoryRepository.findByCategoryType(SkillCategoryType.DEVELOPMENT)
                .orElseGet(() -> skillCategoryRepository.save(SkillCategory.create(SkillCategoryType.DEVELOPMENT)));
    }

    private UserSkill createUserSkill(SkillCategory skillCategory, int exchangeDuration) {
        return UserSkill.create(
                skillCategory,
                "name",
                "title",
                SkillProficiency.MEDIUM,
                "description",
                exchangeDuration
        );
    }

    private SkillExchange createSavedExchange(User requester, User receiver, UserSkill receiverSkill,
                                              LocalDate scheduleDate, LocalTime start, LocalTime end) {
        SkillExchange skillExchange = SkillExchange.create(
                requester,
                receiver,
                receiverSkill,
                scheduleDate,
                start,
                end,
                "defaultMessage"
        );

        return skillExchangeRepository.save(skillExchange);
    }

    private Credit createSavedCredit(User user, int amount) {
        Credit credit = Credit.create(user, amount);
        return creditRepository.save(credit);
    }

    private User createSavedUser() {
        String uuid = UUID.randomUUID().toString();
        User user = User.create(
                OAuthProvider.KAKAO,
                "kakao_1" + uuid,
                uuid + "@test.com",
                "tester" + uuid,
                "https://image",
                "testerNickname" + uuid);

        return userRepository.save(user);
    }
}
