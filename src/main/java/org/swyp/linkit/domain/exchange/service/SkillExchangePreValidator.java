package org.swyp.linkit.domain.exchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.domain.exchange.dto.SkillExchangeDto;
import org.swyp.linkit.domain.user.dto.ExpandedScheduleDto;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.service.AvailableScheduleService;
import org.swyp.linkit.domain.user.service.UserService;
import org.swyp.linkit.domain.user.service.UserSkillService;
import org.swyp.linkit.global.error.exception.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.swyp.linkit.domain.exchange.entity.SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;


@Component
@RequiredArgsConstructor
public class SkillExchangePreValidator {

    private final UserService userService;
    private final UserSkillService userSkillService;
    private final CreditService creditService;
    private final AvailableScheduleService availableScheduleService;

    /**
     * 락 이전에 실행하는 검증
     */
    @Transactional(readOnly = true)
    public Set<LocalTime> preValidate(Long requesterId, SkillExchangeDto dto) {

        // 1. 멘티, 멘토 조회 및 검증 → UserNotFoundException
        userService.getUserById(requesterId);
        User mentor = userService.getUserById(dto.getReceiverId());

        // 2. 멘토 스킬 조회 (락 없이) → UserSkillNotFoundException
        UserSkill mentorSkill = userSkillService.getUserSkillWithProfileAndUser(dto.getReceiverSkillId());

        // 3. 스킬 소유자 검증 → SkillMentorMissMatchException
        if (!mentorSkill.getUserProfile().getUser().getId().equals(mentor.getId())) {
            throw new SkillMentorMissMatchException();
        }

        // 4. 스킬 공개 여부 검증 → SkillNotAvailableException
        if (!mentorSkill.getIsVisible()) {
            throw new SkillNotAvailableException();
        }

        // 5. 본인 신청 방지 → SelfExchangeNotAllowedException
        if (requesterId.equals(dto.getReceiverId())) {
            throw new SelfExchangeNotAllowedException();
        }

        // 6. 크레딧 잔액 검증 → NotFoundCreditException, NotEnoughCreditException
        int amount = mentorSkill.getExchangeDuration() / CREDIT_EXCHANGE_RATE_MINUTES;
        creditService.validateAvailableBalance(requesterId, amount);

        // 7. 멘토 운영 슬롯 조회 — 91일 루프 (락 밖에서 실행)
        return getOperatingSlots(dto.getReceiverId(), dto.getRequestedDate());
    }

    /**
     * 멘토의 특정 날짜의 가능 스케줄을 30분 단위 슬롯으로 변환
     */
    private Set<LocalTime> getOperatingSlots(Long mentorId, LocalDate date) {
        List<ExpandedScheduleDto> expandedSchedules = availableScheduleService
                .getExpandedSchedules(mentorId)
                .stream()
                .filter(dto -> dto.getDate().equals(date))
                .toList();

        Set<LocalTime> totalOperatingSlots = new HashSet<>();
        for (ExpandedScheduleDto expanded : expandedSchedules) {
            LocalTime start = expanded.getStartTime();
            LocalTime end   = expanded.getEndTime();
            while (start.isBefore(end) || (end.equals(LocalTime.MIDNIGHT) && !start.equals(LocalTime.MIDNIGHT))) {
                totalOperatingSlots.add(start);
                start = start.plusMinutes(30);
            }
        }
        return totalOperatingSlots;
    }
}
