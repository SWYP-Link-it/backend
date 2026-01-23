package org.swyp.linkit.domain.notification.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class UnreadCountResponseDto {

    // 요청 관리 탭 - 전체 미읽음 (받은 요청 + 보낸 요청 + 상태 변경)
    private long requestTabCount;

    // 받은 요청 탭 - 미읽음 개수
    private long receivedRequestCount;

    // 보낸 요청 탭 - 미읽음 개수 (보낸 요청 + 상태 변경)
    private long sentRequestCount;

    // 메시지 탭 - 전체 미읽음
    private long messageTabCount;

    public static UnreadCountResponseDto of(long requestTabCount, long receivedRequestCount,
                                             long sentRequestCount, long messageTabCount) {
        return UnreadCountResponseDto.builder()
                .requestTabCount(requestTabCount)
                .receivedRequestCount(receivedRequestCount)
                .sentRequestCount(sentRequestCount)
                .messageTabCount(messageTabCount)
                .build();
    }
}