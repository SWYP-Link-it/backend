package org.swyp.linkit.domain.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.credit.entity.HistoryType;
import org.swyp.linkit.domain.credit.service.CreditService;
import org.swyp.linkit.domain.exchange.dto.SkillExchangeDto;
import org.swyp.linkit.domain.exchange.dto.response.*;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.SkillExchangeRepository;
import org.swyp.linkit.domain.exchange.repository.projection.SkillExchangeDetailQuery;
import org.swyp.linkit.domain.settlement.service.SettlementService;
import org.swyp.linkit.domain.user.dto.ExpandedScheduleDto;
import org.swyp.linkit.domain.user.dto.response.UserSkillForExchangeDto;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.service.AvailableScheduleService;
import org.swyp.linkit.domain.user.service.UserService;
import org.swyp.linkit.domain.user.service.UserSkillService;
import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.swyp.linkit.domain.exchange.entity.SkillExchange.CREDIT_EXCHANGE_RATE_MINUTES;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillExchangeServiceImpl implements SkillExchangeService {

    private final SkillExchangeRepository exchangeRepository;
    private final AvailableScheduleService availableScheduleService;
    private final UserService userService;
    private final UserSkillService userSkillService;
    private final CreditService creditService;
    private final SettlementService settlementService;
    private final SkillExchangeExpireProcessor exchangeExpireProcessor;

    @Transactional(readOnly = true)
    @Override
    public List<UserSkillForExchangeDto> getSkills(Long mentorId) {
        // 1. 멘토 존재 여부 검증 -> MentorNotFound Exception
        userService.getUserById(mentorId);
        // 2. 멘토의 공개된 스킬 조회
        return userSkillService.getSkillsForExchange(mentorId);
    }

    /**
     * 멘토의 거래 가능 날짜 조회
     */
    @Transactional(readOnly = true)
    @Override
    public AvailableDatesResponseDto getAvailableDates(Long mentorId, String yearMonth) {
        // 1. 멘토 존재 여부 검증 -> MentorNotFound Exception
        getMentorAndValidation(mentorId);

        // 2. 멘토의 2일 뒤 ~ 3달 까지의 가능한 날짜 조회 (등록된 스케줄이 없다면 List.of() 반환) 및 월별 필터링
        List<String> filteredSchedules = availableScheduleService.getExpandedSchedules(mentorId).stream()
                .map(dto -> dto.getDate().toString())
                .filter(date -> date.startsWith(yearMonth))
                .distinct()
                .sorted()
                .toList();

        // 3. 가능한 날짜가 존재 검증 -> ScheduleNotFoundException
        if (filteredSchedules.isEmpty()) {
            throw new ScheduleNotFoundException(yearMonth + "해당 월에 멘토의 스케줄이 존재하지 않습니다.");
        }

        // 4. 응답 Dto 변환
        return AvailableDatesResponseDto.of(yearMonth, filteredSchedules);
    }

    /**
     * 멘토의 날짜 별 거래 가능 시간 조회
     */
    @Transactional(readOnly = true)
    @Override
    public AvailableSlotsResponseDto getAvailableSlots(Long mentorId, Long receiverSkillId, LocalDate date) {
        // 1.멘토의 스킬, 멘토 조회 및 존재 여부 검증 -> UserSkillNotFound, SkillMentorMissMatchException Exception
        UserSkill mentorSkill = getMentorSkillAndValidation(mentorId, receiverSkillId);
        int exchangeDuration = mentorSkill.getExchangeDuration();

        // 2. 멘토의 가능 시간 조회 및 date로 필터링, 30분 단위로 변환
        Set<LocalTime> operatingSlots = getOperatingSlots(mentorId, date);
        // 3. date 기준 멘토의 예약 조회 및 30분 단위로 변환
        Set<LocalTime> bookedSlots = getBookedSlots(mentorId, date);

        // 4. exchangeDuration 기준 예약 가능한 시간 처리
        List<SlotDto> finalSlots = calculateAvailableSlots(operatingSlots, exchangeDuration, bookedSlots);

        return AvailableSlotsResponseDto.of(date.toString(), finalSlots);
    }

    /**
     * 스킬 거래 요청
     */
    @Transactional
    @Override
    public SkillExchangeResponseDto requestSkillExchange(Long requesterId, SkillExchangeDto dto) {
        // 1. 멘티, 멘토 조회 및 검증
        User mentee = userService.getUserById(requesterId);
        User mentor = userService.getUserById(dto.getReceiverId());

        // 2. 멘토의 스킬 조회 및 존재 여부 검증 -> UserSkillNotFound
        UserSkill mentorSkill = getMentorSkillWithLockAndValidation(dto.getReceiverSkillId());

        // 3. 멘토의 스킬과 멘토 정보가 일치하는지 검증 -> SkillMentorMissMatchException
        if (!mentorSkill.getUserProfile().getUser().getId().equals(mentor.getId())) {
            throw new SkillMentorMissMatchException();
        }

        // 4. 공개된 skill인지 검증
        if (!mentorSkill.getIsVisible()) {
            throw new SkillNotAvailableException();
        }

        // 5. 본인 신청 방지
        if (requesterId.equals(dto.getReceiverId())) {
            throw new SelfExchangeNotAllowedException();
        }

        // 6.멘티의 크레딧 잔액이 요청 금액보다 부족한지 검증 -> NotFoundCreditException
        int amount = mentorSkill.getExchangeDuration() / CREDIT_EXCHANGE_RATE_MINUTES;
        creditService.validateAvailableBalance(requesterId, amount);

        // 7. 시작 시간, 종료 시간 계산
        LocalTime startTime = dto.getStartTime();
        LocalTime endTime = startTime.plusMinutes(mentorSkill.getExchangeDuration());

        // 8. 신청한 시간(startTime ~ endTime)이 가능한지 검증
        // 멘토의 가능 시간 조회 및 date로 필터링, 30분 단위로 변환
        Set<LocalTime> operatingSlots = getOperatingSlots(dto.getReceiverId(), dto.getRequestedDate());
        // date 기준 멘토의 예약 조회 및 30분 단위로 변환
        Set<LocalTime> bookedSlots = getBookedSlots(dto.getReceiverId(), dto.getRequestedDate());

        // 9. 신청한 시간대(startTime ~ endTime)가 유효한지 검증
        validateTimeSlotAvailability(startTime, endTime, operatingSlots, bookedSlots, mentorSkill.getExchangeDuration());

        // 10. SkillExchange 생성 및 save
        SkillExchange skillExchange = SkillExchange.create(
                mentee,
                mentor,
                mentorSkill,
                dto.getRequestedDate(),
                startTime,
                endTime,
                dto.getMessage()
        );
        SkillExchange savedSkillExchange = exchangeRepository.save(skillExchange);

        // 11. 멘티의 크레딧 차감 및 크레딧 사용 내역 생성
        creditService.useCreditForExchangeRequest(savedSkillExchange);

        // 12. 응답 Dto 변환 및 return
        return SkillExchangeResponseDto.from(savedSkillExchange);
    }

    /**
     * 스킬 거래 요청 조회 - 보낸 요청
     */
    @Transactional
    @Override
    public SkillExchangeDetailsResponseDto getSentRequests(Long userId, Long cursorId, int size) {
        // 1. Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, size);

        // 2. 스킬 거래 요청 내역 커서 기반 페이징 조회
        Slice<SkillExchangeDetailQuery> slice =
                exchangeRepository.findAllByRequesterIdWithReceiver(userId, cursorId, pageable);

        // 3. 응답 Dto 변환
        SkillExchangeDetailsResponseDto responseDto = SkillExchangeDetailsResponseDto.from(slice);

        // 4. bulkUpdate (isRequesterRead = false -> true)
        exchangeRepository.bulkUpdateRequesterReadStatus(userId);
        return responseDto;
    }

    /**
     * 스킬 거래 요청 조회 - 받은 요청
     */
    @Transactional
    @Override
    public SkillExchangeDetailsResponseDto getReceivedRequests(Long userId, Long cursorId, int size) {
        // 1. Pageable 객체 생성
        Pageable pageable = PageRequest.of(0, size);

        // 2. 스킬 거래 요청 내역 커서 기반 페이징 조회
        Slice<SkillExchangeDetailQuery> slice =
                exchangeRepository.findAllByReceiverIdWithRequester(userId, cursorId, pageable);

        // 3. 응답 Dto 변환
        SkillExchangeDetailsResponseDto responseDto = SkillExchangeDetailsResponseDto.from(slice);

        // 4.bulkUpdate (isReceiverRead = false -> true)
        exchangeRepository.bulkUpdateReceiverReadStatus(userId);
        return responseDto;
    }

    /**
     * 스킬 거래 수락
     */
    @Transactional
    @Override
    // 완료
    public SkillExchangeResponseDto acceptSkillExchange(Long receiverId, Long skillExchangeId) {
        // 1. SkillExchange 조회 및 검증 -> ExchangeNotFoundException
        SkillExchange skillExchange = getSkillExchangeWithReceiver(skillExchangeId);

        // 2. 해당 스킬 거래가 receiver의 거래 여부 검증 -> ExchangeAccessDeniedException
        validateReceiverAuthority(receiverId, skillExchange);

        // 3. 상태 변경 가능 여부 검증 및 수락 처리 -> InvalidExchangeStatus
        skillExchange.accept();

        // 4. requester 에게 변경 사항 표시
        skillExchange.updateRequesterReadToFalse();

        // 5. Settlement 생성
        settlementService.createSettlement(skillExchange);

        // 5. 응답 Dto 변환
        return SkillExchangeResponseDto.from(skillExchange);
    }

    /**
     * 스킬 거래 거절
     */
    @Transactional
    @Override
    // 완료
    public SkillExchangeResponseDto rejectSkillExchange(Long receiverId, Long skillExchangeId) {
        // 1. SkillExchange (Receiver, Requester Fetch Join) -> ExchangeNotFoundException
        SkillExchange skillExchange = getSkillExchangeWithReceiverAndRequester(skillExchangeId);

        // 2. 해당 스킬 거래가 receiver의 거래 여부 검증 -> ExchangeAccessDeniedException
        validateReceiverAuthority(receiverId, skillExchange);

        // 3. 상태 변경 가능 여부 검증 및 거절 처리 -> InvalidExchangeStatus
        skillExchange.reject();

        // 4. requester 에게 변경 사항 표시
        skillExchange.updateRequesterReadToFalse();

        // 5. requester 크레딧 환불 -> NotFoundCreditException, InvalidCreditAmountException
        creditService.refundCreditForExchange(skillExchange, HistoryType.EXCHANGE_REJECTED);

        // 5. 응답 Dto 변환
        return SkillExchangeResponseDto.from(skillExchange);
    }

    /**
     *  스킬 거래 취소
     *  멘토, 멘티 모두 함께 처리
     */
    @Transactional
    @Override
    public SkillExchangeResponseDto cancelSkillExchange(Long userId, Long skillExchangeId) {
        // 1. skillExchange 조회 (Receiver, Requester Fetch Join) -> ExchangeNotFoundException
        SkillExchange skillExchange = getSkillExchangeWithReceiverAndRequester(skillExchangeId);

        // 2. 거래 취소 요청이 해당 유저의 skillExchange 여부 검증 -> ExchangeAccessDeniedException
        validateCancelAuthority(userId, skillExchange);
        // 3. 거래 취소 가능 기한 검증 -> ExchangeCancelNotAllowedException
        validateCancelDeadline(skillExchange);

        // 4. requester, receiver 구분 및 상태 검증 및 읽음 업데이트 -> InvalidExchangeStatusException
        processParticipantCancel(userId, skillExchange);

        // 5. 취소 처리
        skillExchange.cancel();

        // 6. requester 크레딧 환불 -> NotFoundCreditException, InvalidCreditAmountException
        creditService.refundCreditForExchange(skillExchange, HistoryType.EXCHANGE_CANCELED);

        return SkillExchangeResponseDto.from(skillExchange);
    }

    /**
     *  거래 날짜 전날까지 수락되지 않은 요청 만료(거절) 처리(expired)
     */
    @Transactional(readOnly = true)
    @Override
    public int expirePendingRequests() {
        LocalDate today = LocalDate.now();

        // 1. expired 처리 대상 목록 조회 (Requester, Receiver Fetch Join)
        List<Long> expiredTargetIds = exchangeRepository.findAllExpiredTargets(today, ExchangeStatus.PENDING);
        if(expiredTargetIds.isEmpty()){
            log.info("만료 처리 대상 없음");
            return 0;
        }

        // 2. 각 거래 독립적인 트랜잭션으로 처리
        int successCount = 0;
        for (Long exchangeId : expiredTargetIds) {
            try {
                // REQUIRES_NEW
                exchangeExpireProcessor.expireSingleSkillExchange(exchangeId);
                successCount++;
            } catch (Exception e){
                log.error("거래 만료 처리 실패. skillExchangeId: {}, errorMessage: {}", exchangeId, e.getMessage());
            }
        }
        return successCount;
    }

    /**
     *  요청 관리 네비바, 탭에 사용할 신규 알림 표시
     *
     */
    @Transactional
    @Override
    public SkillExchangeNotificationResponseDto getNotification(Long userId) {
        boolean hasUnreadSent = exchangeRepository.existsByRequester_IdAndIsRequesterReadFalse(userId);
        boolean hasUnreadReceived = exchangeRepository.existsByReceiver_IdAndIsReceiverReadFalse(userId);
        return SkillExchangeNotificationResponseDto.of(hasUnreadSent, hasUnreadReceived);
    }

    // == private Methods ==

    /**
     *  skillExchangeId로 조회 및 Receiver, Requester Fetch Join
     */
    public SkillExchange getSkillExchangeWithReceiverAndRequester(Long skillExchangeId){
        return exchangeRepository.findByIdWithRequesterAndReceiver(skillExchangeId)
                .orElseThrow(() -> new ExchangeNotFoundException());
    }

    /**
     *   requester, receiver 구분 및 상태 검증 및 읽음 업데이트
     */
    private void processParticipantCancel(Long userId, SkillExchange skillExchange) {
        ExchangeStatus currentStatus = skillExchange.getExchangeStatus();
        if(skillExchange.getRequester().getId().equals(userId)){
            // requester -> PENDING, ACCEPTED일 때만 취소 가능
            if (currentStatus != ExchangeStatus.PENDING && currentStatus != ExchangeStatus.ACCEPTED) {
                throw new InvalidExchangeStatusException("대기중, 수락된 거래만 취소가 가능합니다.");
            }
            // settlement 취소 처리
            if (currentStatus == ExchangeStatus.ACCEPTED){
                settlementService.cancelSettlement(skillExchange.getId());
            }
            skillExchange.updateReceiverReadToFalse();
        } else{
            // receiver -> ACCEPTED일 때만 취소 가능
            if (currentStatus != ExchangeStatus.ACCEPTED) {
                throw new InvalidExchangeStatusException("수락된 거래만 취소가 가능합니다.");
            }
            settlementService.cancelSettlement(skillExchange.getId());
            skillExchange.updateRequesterReadToFalse();
        }
    }

    /**
     *  거래 취소 요청이 해당 유저의 skillExchange 여부 검증
     */
    private void validateCancelAuthority(Long userId, SkillExchange skillExchange) {
        boolean isRequester = skillExchange.getRequester().getId().equals(userId);
        boolean isReceiver = skillExchange.getReceiver().getId().equals(userId);

        if (!isRequester && !isReceiver) {
            throw new ExchangeAccessDeniedException(ErrorCode.EXCHANGE_ACCESS_DENIED_CANCEL);
        }
    }

    /**
     *  거래 취소 가능 기한 검증 -> ExchangeCancelNotAllowedException
     *  거래 당일 부터 취소 불가
     */
    private void validateCancelDeadline(SkillExchange skillExchange) {
        if (!LocalDate.now().isBefore(skillExchange.getScheduledDate())) {
            throw new ExchangeCancelNotAllowedException("거래 당일 또는 이후에는 취소가 불가능합니다.");
        }
    }

    /**
     *  해당 스킬 거래가 receiver의 거래 여부 검증 -> ExchangeAccessDeniedException
     */
    private void validateReceiverAuthority(Long receiverId, SkillExchange skillExchange) {
        if (!skillExchange.getReceiver().getId().equals(receiverId)) {
            throw new ExchangeAccessDeniedException(ErrorCode.EXCHANGE_ACCESS_DENIED_ACCEPT_REJECT);
        }
    }

    /**
     *  requester 맞는지 검증 -> ExchangeAccessDeniedException
     */
    private void validateRequesterAuthority(Long requester, SkillExchange skillExchange) {
        if (!skillExchange.getRequester().getId().equals(requester)) {
            throw new ExchangeAccessDeniedException(ErrorCode.EXCHANGE_ACCESS_DENIED_CANCEL);
        }
    }

    /**
     * SkillExchange 조희 및 검증 - Receiver Fetch Join
     */
    private SkillExchange getSkillExchangeWithReceiver(Long skillExchangeId) {
        return exchangeRepository.findByIdWithReceiver(skillExchangeId)
                .orElseThrow(() -> new ExchangeNotFoundException());
    }

    /**
     * SkillExchange 조희 및 검증 - Requester Fetch Join
     */
    private SkillExchange getSkillExchangeWithRequester(Long skillExchangeId) {
        return exchangeRepository.findByIdWithRequester(skillExchangeId)
                .orElseThrow(() -> new ExchangeNotFoundException());
    }

    /**
     * 신청한 시간대(startTime ~ endTime)가 유효한지 검증
     */
    private void validateTimeSlotAvailability(LocalTime start, LocalTime end, Set<LocalTime> operating,
                                              Set<LocalTime> booked, int exchangeDuration) {
        // 스킬 교환은 선택한 날짜의 자정까지 완료되어야한다.
        if (start.isAfter(end) && !end.equals(LocalTime.MIDNIGHT)) {
            throw new OverExchangeDurationMidnightException();
        }

        // 30분 단위로 쪼개서 가능한 슬릇 확인
        int slotsNeeded = exchangeDuration / 30;
        LocalTime checkSlot = start;

        for (int i = 0; i < slotsNeeded; i++) {
            // 멘토가 설정한 가능한 시간 충족 여부 검증
            if (!operating.contains(checkSlot)) {
                throw new UnavailableExchangeTimeException();
            }
            // 이미 예약된 시간 여부 검증
            if (booked.contains(checkSlot)) {
                throw new AlreadyBookedExchangeTimeException();
            }
            checkSlot = checkSlot.plusMinutes(30);
        }
    }

    /**
     * 멘토의 스킬 존재 여부 검증
     * 비관적 락 적용
     */
    private UserSkill getMentorSkillWithLockAndValidation(Long receiverSkillId) {
        // 멘토의 스킬 조회 및 검증 -> UserSkillNotFoundException
        // 비관적 락 적용
        return userSkillService.getUserSkillWithProfileAndUserAndLock(receiverSkillId);
    }

    /**
     * 멘토의 스킬 존재 여부 검증
     */
    private UserSkill getMentorSkillAndValidation(Long mentorId, Long receiverSkillId) {
        // 멘토의 스킬 존재 여부 조회 및 검증 -> UserSkillNotFoundException
        UserSkill mentorSkill = userSkillService.getUserSkillWithProfileAndUser(receiverSkillId);
        // 멘토의 스킬과 멘토 정보가 일치하는지 검증 -> SkillMentorMissMatchException
        if (!mentorSkill.getUserProfile().getUser().getId().equals(mentorId)) {
            throw new SkillMentorMissMatchException();
        }
        return mentorSkill;
    }

    /**
     * 멘토 조회 및 존재 여부 검증 -> UserNotFoundException -> MentorNotFoundException
     */
    private void getMentorAndValidation(Long mentorId) {
        try {
            userService.getUserById(mentorId);
        } catch (UserNotFoundException e) {
            throw new MentorNotFoundException();
        }
    }

    /**
     * date 기준 멘토의 예약 조회 및 30분 단위로 변환
     */
    private Set<LocalTime> getBookedSlots(Long mentorId, LocalDate date) {
        // date 기준 멘토의 예약 조회
        List<ExchangeStatus> activeStatuses = List.of(
                ExchangeStatus.PENDING,
                ExchangeStatus.ACCEPTED,
                ExchangeStatus.COMPLETED,
                ExchangeStatus.SETTLED
        );
        List<SkillExchange> bookedExchanges = exchangeRepository
                .findAllByReceiverIdAndDate(mentorId, date, activeStatuses);

        // 조회된 예약을 30분 단위로 변환
        Set<LocalTime> bookedSlots = new HashSet<>();
        for (SkillExchange exchange : bookedExchanges) {
            LocalTime start = exchange.getStartTime();
            LocalTime end = exchange.getEndTime();

            while (start.isBefore(end)) {
                bookedSlots.add(start);
                start = start.plusMinutes(30);
            }
        }
        return bookedSlots;
    }

    /**
     * 멘토의 특정 날짜의 가능 스케줄을 30분 단위로 변환
     */
    private Set<LocalTime> getOperatingSlots(Long mentorId, LocalDate date) {
        // 멘토의 2일 뒤 ~ 3달 까지의 가능한 날짜 조회 (등록된 스케줄이 없다면 List.of() 반환), 멘토의 특정 날짜의 가능 스케줄 필터링
        List<ExpandedScheduleDto> expandedSchedules = availableScheduleService.getExpandedSchedules(mentorId).stream()
                .filter(dto -> dto.getDate().equals(date))
                .toList();

        Set<LocalTime> totalOperatingSlots = new HashSet<>();
        for (ExpandedScheduleDto expanded : expandedSchedules) {
            LocalTime start = expanded.getStartTime();
            LocalTime end = expanded.getEndTime();

            while (start.isBefore(end)) {
                totalOperatingSlots.add(start);
                start = start.plusMinutes(30);
            }
        }
        return totalOperatingSlots;
    }

    /**
     * exchangeDuration 기준 예약 가능한 시간 처리
     */
    private List<SlotDto> calculateAvailableSlots(Set<LocalTime> operatingSlots, int exchangeDuration, Set<LocalTime> bookedSlots) {
        return operatingSlots.stream()
                .sorted()
                // startTime이 exchangeDuration만큼 시간을 확보했는지 필터링
                .filter(start -> isPossibleSlot(start, exchangeDuration, bookedSlots, operatingSlots))
                .map(start -> SlotDto.of(start, exchangeDuration))
                .toList();
    }

    /**
     * 특정 시작 시간부터 exchangeDuration 동안
     * 모든 슬롯이 비어있고 멘토가 운영 중(operating O)인지 확인
     */
    private boolean isPossibleSlot(LocalTime start,
                                   int exchangeDuration,
                                   Set<LocalTime> bookedSlots,
                                   Set<LocalTime> totalOperatingSlots) {

        int slotsNeeded = exchangeDuration / 30;

        for (int i = 0; i < slotsNeeded; i++) {
            LocalTime current = start.plusMinutes(i * 30);
            if (bookedSlots.contains(current) || !totalOperatingSlots.contains(current)) {
                return false;
            }
        }
        return true;
    }
}