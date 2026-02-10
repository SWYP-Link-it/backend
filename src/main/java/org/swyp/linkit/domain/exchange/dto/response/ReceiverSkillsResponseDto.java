package org.swyp.linkit.domain.exchange.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.user.dto.response.UserSkillForExchangeDto;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "교환용 스킬 정보 응답 DTO")
public class ReceiverSkillsResponseDto {

    @Schema(description = "멘토의 닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "스킬 ID", example = "1")
    private Long skillId;

    @Schema(description = "스킬명", example = "React")
    private String skillName;

    @Schema(description = "스킬 거래 시간 (분)", example = "60")
    private Integer exchangeDuration;

    @Schema(description = "크레딧 가격 (30분당 1크레딧)", example = "2")
    private Integer creditPrice;

    public static ReceiverSkillsResponseDto of(UserSkillForExchangeDto dto, String nickname) {
        return ReceiverSkillsResponseDto.builder()
                .nickname(nickname)
                .skillId(dto.getSkillId())
                .skillName(dto.getSkillName())
                .exchangeDuration(dto.getExchangeDuration())
                .creditPrice(dto.getCreditPrice())
                .build();
    }
}
