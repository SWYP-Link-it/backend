package org.swyp.linkit.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.user.entity.ExchangeType;
import org.swyp.linkit.domain.user.entity.PreferredRegion;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "사용자 프로필 생성 요청")
public class UserProfileCreateRequestDto {

    @Schema(description = "경력 및 경험 (최대 100자)", example = "프론트엔드 개발 5년차입니다.")
    @Size(max = 100, message = "경력 및 경험은 100자 이하여야 합니다.")
    private String experienceDescription;

    @Schema(description = "교환 방식 (ONLINE, OFFLINE, BOTH)", example = "BOTH")
    @NotNull(message = "교환 방식은 필수입니다.")
    private ExchangeType exchangeType;

    @Schema(description = "선호 지역 (오프라인 선택 시)", example = "SEOUL")
    private PreferredRegion preferredRegion;

    @Schema(description = "세부 위치 (최대 15자)", example = "강남역 부근")
    @Size(max = 15, message = "세부 위치는 15자 이하여야 합니다.")
    @Pattern(
            regexp = "^[a-zA-Z가-힣ㄱ-ㅎㅏ-ㅣ0-9 ]+$",
            message = "세부 위치는 한글, 영어, 숫자만 입력 가능합니다."
    )
    private String detailedLocation;

    @Schema(description = "등록할 스킬 목록")
    @Valid
    @NotEmpty(message = "최소 1개 이상의 스킬을 등록해야 합니다.")
    private List<UserSkillRequestDto> skills;

    @Schema(description = "교환 가능 시간대 목록")
    @Valid
    @NotEmpty(message = "최소 1개 이상의 가능 시간을 등록해야 합니다.")
    private List<AvailableScheduleRequestDto> availableSchedules;
}