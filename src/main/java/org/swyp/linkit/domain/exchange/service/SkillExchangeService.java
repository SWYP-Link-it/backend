package org.swyp.linkit.domain.exchange.service;

import org.swyp.linkit.domain.exchange.dto.SkillExchangeDto;
import org.swyp.linkit.domain.exchange.dto.response.*;

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
    /**
     *  스킬 거래 신청
     */
    SkillExchangeResponseDto requestSkillExchange(Long requesterId, SkillExchangeDto dto);
    /**
     *  스킬 거래 요청 조회 - 보낸 요청
     */
    SkillExchangeDetailsResponseDto getSentRequests(Long userId, Long cursorId, int size);
    /**
     *  스킬 거래 요청 조회 - 받은 요청
     */
    SkillExchangeDetailsResponseDto getReceivedRequests(Long userId, Long cursorId, int size);
    /**
     *  스킬 거래 수락
     */
    SkillExchangeResponseDto acceptSkillExchange(Long receiverId, Long skillExchangeId);
    /**
     *  스킬 거래 거절
     */
    SkillExchangeResponseDto rejectSkillExchange(Long receiverId, Long skillExchangeId);
    /**
     *  스킬 거래 취소
     */
    SkillExchangeResponseDto cancelSkillExchange(Long userId, Long skillExchangeId);
    /**
     *  요청 관리 네비바, 탭에 사용할 신규 알림 표시
     */
    SkillExchangeNotificationResponseDto getNotification(Long userId);




}
