package org.swyp.linkit.domain.search.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "인기 검색어 응답")
public class PopularKeywordDto {

    @Schema(description = "순위", example = "1")
    private Integer rank;

    @Schema(description = "검색어", example = "React")
    private String keyword;

    public static PopularKeywordDto of(Integer rank, String keyword) {
        return PopularKeywordDto.builder()
                .rank(rank)
                .keyword(keyword)
                .build();
    }
}