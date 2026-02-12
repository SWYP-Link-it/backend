package org.swyp.linkit.domain.exchange.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.projection.SkillExchangeDetailQuery;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "스킬 거래 요청 상세 페이징 응답")
public class SkillExchangeDetailsResponseDto {

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "다음 페이지를 호출할 때 사용할 커서 값", example = "5")
    private Long nextCursor;

    @Schema(description = "스킬 거래 요청 상세 목록")
    private List<SkillExchangeDetailDto> contents;

    // 보낸 요청
    public static SkillExchangeDetailsResponseDto ofSent(Slice<SkillExchangeDetailQuery> slice){
        List<SkillExchangeDetailDto> contents = slice.stream()
                .map(SkillExchangeDetailDto::ofSent)
                .toList();
        return new SkillExchangeDetailsResponseDto(
                slice.hasNext(),
                slice.hasNext() ? getNextCursor(slice) : null,
                contents);
    }

    // 받은 요청
    public static SkillExchangeDetailsResponseDto ofReceived(Slice<SkillExchangeDetailQuery> slice){
        List<SkillExchangeDetailDto> contents = slice.stream()
                .map(SkillExchangeDetailDto::ofReceived)
                .toList();
        return new SkillExchangeDetailsResponseDto(
                slice.hasNext(),
                slice.hasNext() ? getNextCursor(slice) : null,
                contents);
    }

    private static Long getNextCursor(Slice<SkillExchangeDetailQuery> slice) {
        return slice.isEmpty() ? null : slice.getContent().get(slice.getNumberOfElements() - 1).skillExchangeId();
    }
}
