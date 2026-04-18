package org.swyp.linkit.domain.exchange.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;
import org.swyp.linkit.domain.exchange.repository.projection.SentDetailQuery;

import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "요청한 스킬 거래 페이징 응답")
public class SentExchangeDetailsResponseDto {

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "다음 페이지를 호출할 때 사용할 커서 값", example = "5")
    private Long nextCursor;

    @Schema(description = "요청한 스킬 거래 상세 목록")
    private List<SentExchangeDetailDto> contents;

    public static SentExchangeDetailsResponseDto from(Slice<SentDetailQuery> slice, Set<Long> unreadRefIds){
        List<SentExchangeDetailDto> contents = slice.stream()
                .map(q -> SentExchangeDetailDto.from(q, unreadRefIds.contains(q.skillExchangeId())))
                .toList();

        return new SentExchangeDetailsResponseDto(
                slice.hasNext(),
                slice.hasNext() ? getNextCursor(slice) : null,
                contents);
    }

    private static Long getNextCursor(Slice<SentDetailQuery> slice) {
        return slice.isEmpty() ? null : slice.getContent().get(slice.getNumberOfElements() - 1).skillExchangeId();
    }
}
