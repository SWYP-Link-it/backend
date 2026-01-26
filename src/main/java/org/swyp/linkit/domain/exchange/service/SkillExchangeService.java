package org.swyp.linkit.domain.exchange.service;

import org.swyp.linkit.domain.exchange.dto.response.AvailableDatesResponseDto;
import org.swyp.linkit.domain.exchange.dto.response.AvailableSlotsResponseDto;

import java.time.LocalDate;

public interface SkillExchangeService {

    /**
     *  멘토의 거래 가능 날짜 조회
     */
    AvailableDatesResponseDto getAvailableDates(Long mentorId, String month);
    /**
     *  멘토의 날짜 별 거래 가능 시간 조회
     */
    AvailableSlotsResponseDto getAvailableSlots(Long mentorId, Long receiverSkillId, LocalDate date);


}
