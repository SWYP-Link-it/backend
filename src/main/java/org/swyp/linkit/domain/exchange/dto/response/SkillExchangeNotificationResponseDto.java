package org.swyp.linkit.domain.exchange.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "스킬 거래 알림(읽지 않은 요청) 상태 응답")
public class SkillExchangeNotificationResponseDto {

    // 보낸 요청 탭 빨간 점
    @Schema(description = "보낸 요청 탭의 읽지 않은 알림 존재 여부", example = "true")
    private boolean hasUnreadSent;
    // 받은 요청 탭 빨간 점
    @Schema(description = "받은 요청 탭의 읽지 않은 알림 존재 여부", example = "false")
    private boolean hasUnreadReceived;
    // 네비바에 표시될 빨간 점
    @Schema(description = "전체(네비게이션 바) 읽지 않은 알림 존재 여부", example = "true")
    private boolean hasAnyUnread;

    public static SkillExchangeNotificationResponseDto of(boolean hasUnreadSent,
                                                   boolean hasUnreadReceived){
        return new SkillExchangeNotificationResponseDto(
                hasUnreadSent,
                hasUnreadReceived,
                hasUnreadSent || hasUnreadReceived);
    }
}
