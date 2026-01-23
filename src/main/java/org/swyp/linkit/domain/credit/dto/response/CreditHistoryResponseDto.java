package org.swyp.linkit.domain.credit.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;
import org.swyp.linkit.domain.credit.dto.CreditHistoryDetailDto;
import org.swyp.linkit.domain.credit.entity.CreditHistory;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class CreditHistoryResponseDto {

    private boolean hasNext;
    private Long nextCursor;
    private List<CreditHistoryDetailDto> contents;

    public static CreditHistoryResponseDto of(Slice<CreditHistory> slice, String defaultProfileImageUrl) {
        // nextCursor 계산
        Long lastId = slice.isEmpty() ? null : slice.getContent().get(slice.getNumberOfElements() - 1).getId();

        return CreditHistoryResponseDto.builder()
                .hasNext(slice.hasNext())
                .nextCursor(slice.hasNext() ? lastId : null)
                .contents(slice.getContent().stream()
                        .map(ch -> CreditHistoryDetailDto.from(ch, defaultProfileImageUrl))
                        .toList())
                .build();
    }
}
