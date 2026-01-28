package org.swyp.linkit.domain.exchange.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Schema(description = "스킬 거래 요청")
public class SkillExchangeRequestDto {

    @Schema(description = "멘토의 사용자 ID", example = "1")
    @NotNull(message = "멘토의 ID는 필수입니다.")
    private Long mentorId;

    @NotNull(message = "멘토의 스킬 ID는 필수입니다.")
    @Schema(description = "신청하려는 멘토의 스킬의 ID", example = "10")
    private Long mentorSkillId;

    @Schema(description = "멘토에게 전달할 메시지 (최대 50자)", example = "안녕하세요! 잘 부탁드립니다.")
    @Size(max = 50, message = "메시지는 50자 이내로 입력해주세요.")
    private String message;

    @Schema(description = "달력에서 지정한 날짜", example = "2026-02-01")
    @NotNull(message = "교환 날짜는 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd") // 추가: 형식을 딱 정해줌
    private LocalDate requestedDate;

    @Schema(description = "선택한 교환 시작 시간 (30분 단위)", example = "14:30")
    @NotNull(message = "교환 시간은 필수입니다.")
    @JsonFormat(pattern = "HH:mm") // 추가: 24시간 형식
    private LocalTime startTime;
}
