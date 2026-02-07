package org.swyp.linkit.domain.exchange.service;

import org.swyp.linkit.domain.exchange.dto.SkillExchangeDto;
import org.swyp.linkit.domain.exchange.dto.response.*;
import org.swyp.linkit.domain.user.dto.response.UserSkillForExchangeDto;

import java.time.LocalDate;
import java.util.List;

public interface SkillExchangeService {

    /**
     *  멘토의 거래 가능 스킬 목록 조회
     */
    List<UserSkillForExchangeDto> getSkills(Long mentorId);
    /**
     *  멘토의 거래 가능 날짜 조회
     */
    AvailableDatesResponseDto getAvailableDates(Long mentorId, String yearMonth);
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
    /**
     *  거래 날짜 전날까지 수락되지 않은 요청 거절 처리(expired)
     */
    int expirePendingRequests();




}
