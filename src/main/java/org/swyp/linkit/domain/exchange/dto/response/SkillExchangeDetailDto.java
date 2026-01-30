package org.swyp.linkit.domain.exchange.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.projection.SkillExchangeDetailQuery;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserSkill;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "스킬 거래 요청 상세 내역")
public class SkillExchangeDetailDto {

    @Schema(description = "스킬 거래 식별자(ID)", example = "1")
    private Long skillExchangeId;
    @Schema(description = "스킬 거래 상대 유저 식별자(ID)", example = "2")
    private Long targetUserId;
    @Schema(description = "스킬 식별자(ID)", example = "1")
    private Long skillId;
    @Schema(description = "채팅 방 식별자(ID)", example = "1")
    private Long chatRoomId;
    @Schema(description = "상대 유저의 프로필 이미지 url", example = "https://example-image")
    private String targetProfileImageUrl;
    @Schema(description = "상대 유저의 닉네임", example = "홍길동")
    private String targetNickname;
    @Schema(description = "스킬 명", example = "Java")
    private String skillName;
    @Schema(description = "스킬 거래 상태 (대기중, 수락됨, 거절됨, 취소됨)", example = "대기중")
    private String exchangeStatus;
    @Schema(description = "스킬 거래 크레딧 가격", example = "2")
    private int creditPrice;
    @Schema(description = "간단한 메시지", example = "안녕하세요! 잘 부탁드립니다!")
    private String message;
    @Schema(description = "스킬 거래 요청 날짜", example = "2024-01-20")
    private LocalDate requestedDate;
    @Schema(description = "스킬 거래 날짜 및 시간", example = "2024-01-25T12:00:00")
    private LocalDateTime exchangeDateTime;
    @Schema(description = "스킬 거래 시간", example = "60")
    private int exchangeDuration;
    @Schema(
            description = "새로운 상태 업데이트 여부 (true: 신규 알림 있음/빨간 점 표시, false: 확인 완료)",
            example = "true"
    )
    private boolean isNew;

    public static SkillExchangeDetailDto from(SkillExchangeDetailQuery result) {
        return SkillExchangeDetailDto.builder()
                .skillExchangeId(result.skillExchangeId())
                .targetUserId(result.targetUserId())
                .skillId(result.skillId())
                .chatRoomId(result.chatRoomId())
                .targetProfileImageUrl(result.targetProfileImageUrl())
                .targetNickname(result.targetNickname())
                .skillName(result.skillName())
                .exchangeStatus(result.exchangeStatus().getDescription())
                .creditPrice(result.creditPrice())
                .message(result.message())
                .requestedDate(result.createdAt().toLocalDate())
                .exchangeDateTime(result.exchangeDate().atTime(result.exchangeTime()))
                .exchangeDuration(result.exchangeDuration())
                .isNew(!result.isRead())
                .build();
    }
}
