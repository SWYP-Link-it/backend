package org.swyp.linkit.domain.exchange.repository.projection;

import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record SentDetailQuery(
        Long skillExchangeId,
        Long userId,
        Long targetUserId,
        Long skillId,
        Long chatRoomId,
        String targetProfileImageUrl,
        String targetNickname,
        String skillName,
        ExchangeStatus exchangeStatus,
        int creditPrice,
        String message,
        LocalDateTime createdAt,
        LocalDate exchangeDate,
        LocalTime exchangeTime,
        int exchangeDuration,
        Long reviewId
) {
}
