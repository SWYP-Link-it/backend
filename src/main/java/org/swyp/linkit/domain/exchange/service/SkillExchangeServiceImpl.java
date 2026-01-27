package org.swyp.linkit.domain.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.exchange.dto.SkillExchangeDto;
import org.swyp.linkit.domain.exchange.dto.response.AvailableDatesResponseDto;
import org.swyp.linkit.domain.exchange.dto.response.AvailableSlotsResponseDto;
import org.swyp.linkit.domain.exchange.dto.response.SkillExchangeResponseDto;
import org.swyp.linkit.domain.exchange.dto.response.SlotDto;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.SkillExchangeRepository;
import org.swyp.linkit.domain.user.dto.AvailableScheduleDto;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.domain.user.service.AvailableScheduleService;
import org.swyp.linkit.domain.user.service.UserService;
import org.swyp.linkit.domain.user.service.UserSkillService;
import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillExchangeServiceImpl implements SkillExchangeService {

    private final SkillExchangeRepository exchangeRepository;
    private final AvailableScheduleService availableScheduleService;
    private final UserService userService;
    private final UserSkillService userSkillService;

    /**
     * 멘토의 거래 가능 날짜 조회
     */
    @Transactional(readOnly = true)
    @Override
    public AvailableDatesResponseDto getAvailableDates(Long mentorId, String month) {
        // 1. 멘토의 스킬, 멘토 조회 및 존재 여부 검증 -> UserSkillNotFound, MentorNotFound Exception
        getMentorAndValidation(mentorId);

        // 2. 멘토의 2일 뒤 ~ 3달 까지의 가능한 날짜 조회 (등록된 스케줄이 없다면 List.of() 반환) 및 월별 필터링
        List<String> filteredSchedules = availableScheduleService.getExpandedSchedules(mentorId).stream()
                .map(dto -> dto.getDate().toString())
                .filter(date -> date.startsWith(month))
                .distinct()
                .sorted()
                .toList();

        // 3. 가능한 날짜가 존재 검증 -> ScheduleNotFoundException
        if (filteredSchedules.isEmpty()) {
            throw new ScheduleNotFoundException(month + "해당 월에 멘토의 스케줄이 존재하지 않습니다.");
        }

        // 4. 응답 Dto 변환
        return AvailableDatesResponseDto.of(month, filteredSchedules);
    }

    /**
     * 멘토의 날짜 별 거래 가능 시간 조회
     */
    @Transactional(readOnly = true)
    @Override
    public AvailableSlotsResponseDto getAvailableSlots(Long mentorId, Long receiverSkillId, LocalDate date) {
        // 1.멘토의 스킬, 멘토 조회 및 존재 여부 검증 -> UserSkillNotFound, MentorNotFound Exception
        UserSkill mentorSkill = getMentorSkillAndValidation(mentorId, receiverSkillId);
        int exchangeDuration = mentorSkill.getExchangeDuration();

        // 2. 멘토의 가능 시간 조회 및 date로 필터링, 30분 단위로 변환
        List<LocalTime> operatingSlots = getOperatingSlots(mentorId, date);
        // 3. date 기준 멘토의 예약 조회 및 30분 단위로 변환
        Set<LocalTime> bookedSlots = getBookedSlots(mentorId, date);

        // 4. exchangeDuration 기준 예약 가능한 시간 처리
        List<SlotDto> finalSlots = calculateAvailableSlots(operatingSlots, exchangeDuration, bookedSlots);

        return AvailableSlotsResponseDto.of(date.toString(), finalSlots);
    }

    /**
     *  스킬 거래 요청
     */
    @Transactional
    @Override
    public SkillExchangeResponseDto requestSkillExchange(Long requesterId, SkillExchangeDto dto) {
        // 1. 멘티 조회 및 검증
        User mentee = userService.getUserById(requesterId);

        // 2. 멘토의 스킬, 멘토 조회 및 존재 여부 검증 -> UserSkillNotFound, MentorNotFound Exception
        UserSkill mentorSkill = getMentorSkillWithLockAndValidation(dto.getReceiverId(), dto.getReceiverSkillId());
        User mentor = mentorSkill.getUserProfile().getUser();

        // 3. 공개된 skill인지 검증
        if(!mentorSkill.getIsVisible()){
            throw new SkillNotAvailableException();
        }

        // 4. 본인 신청 방지
        if(requesterId.equals(dto.getReceiverId())) {
            throw new SelfExchangeNotAllowedException();
        }

        // 5. 시작 시간, 종료 시간 계산
        LocalTime startTime = dto.getStartTime();
        LocalTime endTime = startTime.plusMinutes(mentorSkill.getExchangeDuration());

        // 6. 신청한 시간(startTime ~ endTime)이 가능한지 검증
        // 멘토의 가능 시간 조회 및 date로 필터링, 30분 단위로 변환
        List<LocalTime> operatingSlots = getOperatingSlots(dto.getReceiverId(), dto.getRequestedDate());
        // date 기준 멘토의 예약 조회 및 30분 단위로 변환
        Set<LocalTime> bookedSlots = getBookedSlots(dto.getReceiverId(), dto.getRequestedDate());

        // 7. 신청한 시간대(startTime ~ endTime)가 유효한지 검증
        validateTimeSlotAvailability(startTime, endTime, operatingSlots, bookedSlots);

        // 8. SkillExchange 생성 및 save
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
        // 9. 응답 Dto 변환 및 return
        return SkillExchangeResponseDto.from(savedSkillExchange);
    }

    // == private Methods ==

    /**
     * 신청한 시간대(startTime ~ endTime)가 유효한지 검증
     */
    private void validateTimeSlotAvailability(LocalTime start, LocalTime end, List<LocalTime> operating, Set<LocalTime> booked) {
        // 스킬 교환은 선택한 날짜의 자정까지 완료되어야한다.
        if (start.isAfter(end) && !end.equals(LocalTime.MIDNIGHT)) {
            throw new OverExchangeDurationMidnightException();
        }
        LocalTime checkSlot = start;

        // startTime ~ endTime 까지 멘토의 가능 시간인지 예약된 시간인지 검증
        while (checkSlot.isBefore(end)) {
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
     *  멘토의 스킬 존재 여부 검증 -> UserSkillNotFoundException
     *  비관적 락 적용
     */
    private UserSkill getMentorSkillWithLockAndValidation(Long mentorId, Long receiverSkillId) {
        // 멘토의 스킬 존재 여부 조회 및 검증 -> UserSkillNotFoundException
        // 비관적 락 적용
        UserSkill mentorSkill = userSkillService.getUserSkillWithProfileAndUserAndLock(receiverSkillId);
        // 멘토의 스킬과 멘토 정보가 일치하는지 검증 -> MentorNotFoundException
        if(!mentorSkill.getUserProfile().getUser().getId().equals(mentorId)){
            throw new MentorNotFoundException("해당 멘토가 보유한 스킬이 아닙니다.");
        }
        return mentorSkill;
    }

    /**
     *  멘토의 스킬 존재 여부 검증 -> UserSkillNotFoundException
     */
    private UserSkill getMentorSkillAndValidation(Long mentorId, Long receiverSkillId) {
        // 멘토의 스킬 존재 여부 조회 및 검증 -> UserSkillNotFoundException
        UserSkill mentorSkill = userSkillService.getUserSkillWithProfileAndUser(receiverSkillId);
        // 멘토의 스킬과 멘토 정보가 일치하는지 검증 -> MentorNotFoundException
        if(!mentorSkill.getUserProfile().getUser().getId().equals(mentorId)){
            throw new MentorNotFoundException("해당 멘토가 보유한 스킬이 아닙니다.");
        }
        return mentorSkill;
    }

    /**
     *  멘토 조회 및 존재 여부 검증 -> UserNotFoundException -> MentorNotFoundException
     */
    private void getMentorAndValidation(Long mentorId) {
        try{
            userService.getUserById(mentorId);
        } catch (UserNotFoundException e){
            throw new MentorNotFoundException();
        }
    }

    /**
     *  date 기준 멘토의 예약 조회 및 30분 단위로 변환
     */
    private Set<LocalTime> getBookedSlots(Long mentorId, LocalDate date) {
        // date 기준 멘토의 예약 조회
        List<SkillExchange> bookedExchanges = exchangeRepository
                .findAllByReceiverIdAndDate(mentorId, date, ExchangeStatus.CANCELED);

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
     *  멘토의 특정 날짜의 가능 스케줄을 30분 단위로 변환
     */
    private List<LocalTime> getOperatingSlots(Long mentorId, LocalDate date) {
        // 멘토의 2일 뒤 ~ 3달 까지의 가능한 날짜 조회 (등록된 스케줄이 없다면 List.of() 반환), 멘토의 특정 날짜의 가능 스케줄 필터링
        List<AvailableScheduleDto> selectedSchedules = availableScheduleService.getExpandedSchedules(mentorId).stream()
                .filter(dto -> dto.getDate().equals(date))
                .toList();
        List<LocalTime> totalOperatingSlots = new ArrayList<>();
        for (AvailableScheduleDto dto : selectedSchedules) {
            LocalTime start = dto.getStartTime();
            LocalTime end = dto.getEndTime();
            while (start.isBefore(end)) {
                totalOperatingSlots.add(start);
                start = start.plusMinutes(30);
            }
        }
        return totalOperatingSlots;
    }

    /**
     *  exchangeDuration 기준 예약 가능한 시간 처리
     */
    private List<SlotDto> calculateAvailableSlots(List<LocalTime> operatingSlots, int exchangeDuration, Set<LocalTime> bookedSlots) {
        return operatingSlots.stream()
                .sorted()
                .map(start -> SlotDto.of(start, isPossibleSlot(start, exchangeDuration, bookedSlots, operatingSlots)))
                .toList();
    }

    /**
     * 특정 시작 시간부터 exchangeDuration 동안
     * 모든 슬롯이 비어있고 멘토가 운영 중(operating O)인지 확인
     */
    private boolean isPossibleSlot(LocalTime start,
                                   int exchangeDuration,
                                   Set<LocalTime> bookedSlots,
                                   List<LocalTime> totalOperatingSlots) {
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
