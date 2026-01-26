package org.swyp.linkit.domain.credit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "크레딧 사용 내역")
public class CreditHistoryDetailDto {

    @Schema(description = "크레딧 사용 내역 식별자(ID)", example = "1")
    private Long id;
    @Schema(description = "스킬 거래 상대 유저 식별자(ID)", example = "2")
    private Long targetUserId;
    @Schema(description = "상대 유저의 스킬 식별자(ID)", example = "1")
    private Long skillId;
    @Schema(description = "상대 유저의 프로필 이미지 url", example = "https://example-image")
    private String targetProfileImageUrl;
    @Schema(description = "상대 유저의 닉네임 (시스템 지급 크레딧일 경우 시스템)", example = "홍길동")
    private String targetNickname;
    @Schema(description = "거래 명 (스킬 교환 시 스킬명, 리워드일 경우 리워드 명)", example = "Java")
    private String contentName;
    @Schema(description = "크레딧 사용 내역 생성 날짜", example = "2024-01-20T12:00:00")
    private LocalDateTime createdAt;
    @Schema(description = "크레딧 사용 내역의 상세 구분(리워드, 요청, 취소, 거절, 만료, 정산)", example = "요청")
    private String statusLabel;
    @Schema(description = "크레딧 변동 금액 (증가: 양수, 사용: 음수)", example = "-2")
    private int changeAmount;

    public static CreditHistoryDetailDto from(CreditHistory history, String defaultProfileImageUrl){
        User targetUser = history.getTargetUser();
        SkillExchange skillExchange = history.getSkillExchange();

        return CreditHistoryDetailDto.builder()
                .id(history.getId())
                .targetUserId(targetUser == null ? null : targetUser.getId())
                .skillId(skillExchange == null ? null : skillExchange.getReceiverSkill().getId())
                .targetProfileImageUrl(targetUser == null ? defaultProfileImageUrl : targetUser.getProfileImageUrl())
                .targetNickname(targetUser == null ? "시스템" : targetUser.getNickname())
                .contentName(history.getContentName())
                .createdAt(history.getCreatedAt())
                .statusLabel(history.getHistoryType().getStatusLabel())
                .changeAmount(history.getChangeAmount())
                .build();
    }
}
