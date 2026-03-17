package org.swyp.linkit.domain.exchange.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;
import org.swyp.linkit.domain.exchange.repository.projection.ReceivedDetailQuery;

import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "받은 스킬 거래 페이징 응답")
public class ReceivedExchangeDetailsResponseDto {

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "다음 페이지를 호출할 때 사용할 커서 값", example = "5")
    private Long nextCursor;

    @Schema(description = "받은 스킬 거래 상세 목록")
    private List<ReceivedExchangeDetailDto> contents;

    public static ReceivedExchangeDetailsResponseDto from(Slice<ReceivedDetailQuery> slice, Set<Long> unreadExchangeIds){
        List<ReceivedExchangeDetailDto> contents = slice.stream()
                .map(q -> ReceivedExchangeDetailDto.from(q, unreadExchangeIds.contains(q.skillExchangeId())))
                .toList();
        return new ReceivedExchangeDetailsResponseDto(
                slice.hasNext(),
                slice.hasNext() ? getNextCursor(slice) : null,
                contents);
    }

    private static Long getNextCursor(Slice<ReceivedDetailQuery> slice) {
        return slice.isEmpty() ? null : slice.getContent().get(slice.getNumberOfElements() - 1).skillExchangeId();
    }
}
