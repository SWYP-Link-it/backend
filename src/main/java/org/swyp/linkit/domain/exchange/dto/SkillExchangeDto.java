package org.swyp.linkit.domain.exchange.dto;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.exchange.dto.request.SkillExchangeRequestDto;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class SkillExchangeDto {

    private Long receiverId;
    private Long receiverSkillId;
    private String message;
    private LocalDate requestedDate;
    private LocalTime startTime;

    @Builder(access = AccessLevel.PRIVATE)
    private SkillExchangeDto(Long mentorId, Long mentorSkillId, String message, LocalDate requestedDate, LocalTime startTime) {
        this.receiverId = mentorId;
        this.receiverSkillId = mentorSkillId;
        this.message = message;
        this.requestedDate = requestedDate;
        this.startTime = startTime;
    }

    public static SkillExchangeDto from(SkillExchangeRequestDto requestDto){
        return SkillExchangeDto.builder()
                .mentorId(requestDto.getMentorId())
                .mentorSkillId(requestDto.getMentorSkillId())
                .message(requestDto.getMessage())
                .requestedDate(requestDto.getRequestedDate())
                .startTime(requestDto.getStartTime())
                .build();
    }
}
