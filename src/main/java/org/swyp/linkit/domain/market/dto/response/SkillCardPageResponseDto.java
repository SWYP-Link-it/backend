package org.swyp.linkit.domain.market.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;
import org.swyp.linkit.domain.user.entity.UserSkill;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "스킬 카드 커서 페이징 응답")
public class SkillCardPageResponseDto {

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "다음 페이지를 호출할 때 사용할 커서 값 (null 이면 마지막 데이터)", example = "10")
    private Long nextCursorId;

    @Schema(description = "스킬 카드 목록")
    private List<SkillCardResponseDto> skills;

    private static final SkillCardPageResponseDto EMPTY = SkillCardPageResponseDto.builder()
            .hasNext(false)
            .nextCursorId(null)
            .skills(List.of())
            .build();

    /**
     * Slice<UserSkill> 을 SkillCardPageResponseDto 로 변환
     * - hasNext 가 true 면 마지막 요소의 id 를 nextCursorId 로 설정
     * - hasNext 가 false 면 nextCursorId 는 null
     */
    public static SkillCardPageResponseDto of(Slice<UserSkill> slice) {
        List<UserSkill> content = slice.getContent();

        Long nextCursorId = slice.hasNext() && !slice.isEmpty()
                ? content.get(slice.getNumberOfElements() - 1).getId()
                : null;

        return SkillCardPageResponseDto.builder()
                .hasNext(slice.hasNext())
                .nextCursorId(nextCursorId)
                .skills(content.stream()
                        .map(SkillCardResponseDto::from)
                        .toList())
                .build();
    }

    /**
     * 빈 결과 반환 (keyword 가 null 이거나 비어있을 때)
     */
    public static SkillCardPageResponseDto empty() {
        return EMPTY;
    }
}