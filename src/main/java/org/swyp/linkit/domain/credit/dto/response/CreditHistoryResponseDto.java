package org.swyp.linkit.domain.credit.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "크레딧 사용 내역 페이징 응답")
public class CreditHistoryResponseDto {

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "다음 페이지를 호출할 때 사용할 커서 값", example = "5")
    private Long nextCursor;

    @Schema(description = "크레딧 사용 내역 목록")
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
