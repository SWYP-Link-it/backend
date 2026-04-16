package org.swyp.linkit.domain.exchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.domain.exchange.dto.SkillExchangeDto;
import org.swyp.linkit.domain.exchange.dto.response.SkillExchangeResponseDto;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.SkillExchangeRepository;
import org.swyp.linkit.domain.notification.entity.NotificationType;
import org.swyp.linkit.domain.notification.service.NotificationService;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.service.UserService;
import org.swyp.linkit.domain.user.service.UserSkillService;
import org.swyp.linkit.global.error.exception.AlreadyBookedExchangeTimeException;
import org.swyp.linkit.global.error.exception.OverExchangeDurationMidnightException;
import org.swyp.linkit.global.error.exception.UnavailableExchangeTimeException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class SkillExchangeRequestProcessor {

    private final SkillExchangeRepository exchangeRepository;
    private final CreditService creditService;
    private final UserService userService;
    private final UserSkillService userSkillService;
    private final NotificationService notificationService;

    /**
     * 처리 순서:
     *   TX 시작
     *   1. 비관적 락 적용 (SELECT ... FOR UPDATE → 락 획득)
     *   2. getBookedSlots() → 최신 예약 현황 재조회 (락 획득 직후)
     *   3. validateTimeSlotAvailability() → 슬롯 충돌 최종 확인
     *   4. SkillExchange 저장
     *   5. 크레딧 차감
     *   TX 커밋 → 락 해제
     */
    @Transactional
    public SkillExchangeResponseDto executeWithLock(Long requesterId,
                                                    SkillExchangeDto dto,
                                                    Set<LocalTime> operatingSlots) {

        // 1. 멘토의 스킬 조회 및 락 획득
        UserSkill mentorSkill = userSkillService.getUserSkillWithProfileAndUserAndLock(dto.getReceiverSkillId());

        // 2. 시작/종료 시간 계산
        LocalTime startTime = dto.getStartTime();
        LocalTime endTime   = startTime.plusMinutes(mentorSkill.getExchangeDuration());

        // 3. 예약 현황 재조회 (락 획득 직후 최신 상태로 확인)
        Set<LocalTime> bookedSlots = getBookedSlots(dto.getReceiverId(), dto.getRequestedDate());

        // 4. 슬롯 충돌 최종 검증
        // -> operatingSlots: preValidate()에서 조회 (멘토 운영 시간은 중간에 바뀌지 않으므로 재사용)
        // -> bookedSlots: 방금 재조회한 최신 상태
        validateTimeSlotAvailability(startTime, endTime, operatingSlots, bookedSlots,
                mentorSkill.getExchangeDuration());

        // 5. SkillExchange 생성 및 저장
        User mentee = userService.getUserById(requesterId);
        User mentor  = userService.getUserById(dto.getReceiverId());

        SkillExchange skillExchange = SkillExchange.create(
                mentee, mentor, mentorSkill,
                dto.getRequestedDate(), startTime, endTime,
                dto.getMessage()
        );
        SkillExchange saved = exchangeRepository.save(skillExchange);

        // 6. 크레딧 차감 및 사용 내역 생성
        creditService.useCreditForExchangeRequest(saved);

        // 7. 알림 생성 (멘토: REQUEST_RECEIVED, 멘티: REQUEST_SENT) — afterCommit 시 Redis 발행
        notificationService.createNotification(
                saved.getReceiver().getId(), saved.getRequester().getId(),
                NotificationType.REQUEST_RECEIVED, saved.getId());
        notificationService.createNotification(
                saved.getRequester().getId(), saved.getReceiver().getId(),
                NotificationType.REQUEST_SENT, saved.getId());

        // TX 커밋 → 락 해제
        return SkillExchangeResponseDto.from(saved);
    }

    /**
     * 해당 날짜에 멘토에게 이미 예약된 슬롯을 30분 단위로 변환하여 반환
     */
    private Set<LocalTime> getBookedSlots(Long mentorId, LocalDate date) {
        List<ExchangeStatus> activeStatuses = List.of(
                ExchangeStatus.PENDING,
                ExchangeStatus.ACCEPTED,
                ExchangeStatus.COMPLETED
        );
        List<SkillExchange> bookedExchanges = exchangeRepository
                .findAllByReceiverIdAndDate(mentorId, date, activeStatuses);

        Set<LocalTime> bookedSlots = new HashSet<>();
        for (SkillExchange exchange : bookedExchanges) {
            LocalTime start = exchange.getStartTime();
            LocalTime end   = exchange.getEndTime();
            while (start.isBefore(end)) {
                bookedSlots.add(start);
                start = start.plusMinutes(30);
            }
        }
        return bookedSlots;
    }

    /**
     * 요청 시간대(startTime ~ endTime)가 예약 가능한지 검증
     * - 운영 슬롯에 포함되는지
     * - 이미 예약된 슬롯과 겹치지 않는지
     */
    private void validateTimeSlotAvailability(LocalTime start, LocalTime end,
                                              Set<LocalTime> operating, Set<LocalTime> booked,
                                              int exchangeDuration) {
        // 교환 시간이 자정을 넘어가는 경우 예외
        if (start.isAfter(end) && !end.equals(LocalTime.MIDNIGHT)) {
            throw new OverExchangeDurationMidnightException();
        }

        int slotsNeeded = exchangeDuration / 30;
        LocalTime checkSlot = start;

        for (int i = 0; i < slotsNeeded; i++) {
            if (!operating.contains(checkSlot)) {
                throw new UnavailableExchangeTimeException();
            }
            if (booked.contains(checkSlot)) {
                throw new AlreadyBookedExchangeTimeException();
            }
            checkSlot = checkSlot.plusMinutes(30);
        }
    }
}
