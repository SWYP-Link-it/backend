package org.swyp.linkit.domain.credit.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class CreditHistoryDetailDto {

    // CreditHistoryId
    private Long id;
    // 상대 User Id
    private Long targetUserId;
    // 거래한 UserSkill Id
    private Long skillId;
    // 상대 User imageUrl
    private String targetProfileImageUrl;
    // 상대 User nickName
    private String targetNickname;
    // 거래 명(스킬 교환일 경우 스킬명, 리워드일 경우 회원가입 혹은 프로필 작성)
    private String contentName;
    // 크레딧 내역 생성 날짜
    private LocalDateTime createdAt;
    // 구분(리워드, 요청, 취소, 거절, 만료, 정산)
    private String statusLabel;
    // 크레딧 변동 금액
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
