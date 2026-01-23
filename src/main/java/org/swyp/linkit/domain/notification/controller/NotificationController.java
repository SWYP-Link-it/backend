package org.swyp.linkit.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.swyp.linkit.domain.notification.dto.response.ChatRoomUnreadCountResponseDto;
import org.swyp.linkit.domain.notification.dto.response.NotificationListResponseDto;
import org.swyp.linkit.domain.notification.dto.response.UnreadCountResponseDto;
import org.swyp.linkit.domain.notification.service.NotificationService;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.common.dto.ApiResponseDto;
import org.swyp.linkit.global.swagger.annotation.ApiErrorExceptionsExample;
import org.swyp.linkit.global.swagger.docs.NotificationExceptionDocs;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    // ===== 미읽음 개수 조회 =====

    @Operation(
            summary = "탭별 미읽음 알림 개수 조회",
            description = "요청 관리 탭, 받은 요청, 보낸 요청, 메시지 탭의 미읽음 알림 개수를 조회합니다."
    )
    @ApiErrorExceptionsExample(NotificationExceptionDocs.class)
    @GetMapping(value = "/unread-count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<UnreadCountResponseDto>> getUnreadCounts(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        UnreadCountResponseDto responseDto = notificationService.getUnreadCounts(oAuth2User.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success("미읽음 알림 개수 조회 성공", responseDto));
    }

    @Operation(
            summary = "특정 채팅방의 미읽음 알림 개수 조회",
            description = "특정 채팅방의 미읽음 메시지 알림 개수를 조회합니다."
    )
    @ApiErrorExceptionsExample(NotificationExceptionDocs.class)
    @GetMapping(value = "/unread-count/chat-rooms/{chatRoomId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<ChatRoomUnreadCountResponseDto>> getChatRoomUnreadCount(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @Parameter(description = "채팅방 ID") @PathVariable Long chatRoomId) {

        ChatRoomUnreadCountResponseDto responseDto = notificationService.getChatRoomUnreadCount(
                oAuth2User.getUserId(), chatRoomId);
        return ResponseEntity.ok(ApiResponseDto.success("채팅방 미읽음 알림 개수 조회 성공", responseDto));
    }

    // ===== 알림 목록 조회 =====

    @Operation(
            summary = "알림 목록 조회",
            description = "사용자의 전체 알림 목록을 최신순으로 조회합니다."
    )
    @ApiErrorExceptionsExample(NotificationExceptionDocs.class)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<NotificationListResponseDto>> getNotifications(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        NotificationListResponseDto responseDto = notificationService.getNotifications(oAuth2User.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success("알림 목록 조회 성공", responseDto));
    }

    // ===== 알림 읽음 처리 =====

    @Operation(
            summary = "요청 관리 페이지 진입 - 모든 요청 알림 읽음 처리",
            description = "요청 관리 페이지에 진입했을 때, 모든 요청 관련 알림(받은 요청, 보낸 요청, 상태 변경)을 읽음 처리합니다."
    )
    @ApiErrorExceptionsExample(NotificationExceptionDocs.class)
    @PostMapping(value = "/read/requests", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Integer>> markRequestNotificationsAsRead(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        int count = notificationService.markRequestNotificationsAsRead(oAuth2User.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success("요청 알림 읽음 처리 성공", count));
    }

    @Operation(
            summary = "받은 요청 탭 진입 - 받은 요청 알림 읽음 처리",
            description = "받은 요청 탭에 진입했을 때, 받은 요청 알림을 읽음 처리합니다."
    )
    @ApiErrorExceptionsExample(NotificationExceptionDocs.class)
    @PostMapping(value = "/read/requests/received", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Integer>> markReceivedRequestAsRead(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        int count = notificationService.markReceivedRequestAsRead(oAuth2User.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success("받은 요청 알림 읽음 처리 성공", count));
    }

    @Operation(
            summary = "보낸 요청 탭 진입 - 보낸 요청 알림 읽음 처리",
            description = "보낸 요청 탭에 진입했을 때, 보낸 요청 및 상태 변경 알림을 읽음 처리합니다."
    )
    @ApiErrorExceptionsExample(NotificationExceptionDocs.class)
    @PostMapping(value = "/read/requests/sent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Integer>> markSentRequestAsRead(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        int count = notificationService.markSentRequestAsRead(oAuth2User.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success("보낸 요청 알림 읽음 처리 성공", count));
    }

    @Operation(
            summary = "메시지 목록 페이지 진입 - 모든 메시지 알림 읽음 처리",
            description = "메시지 목록 페이지에 진입했을 때, 모든 채팅 메시지 알림을 읽음 처리합니다."
    )
    @ApiErrorExceptionsExample(NotificationExceptionDocs.class)
    @PostMapping(value = "/read/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Integer>> markMessageNotificationsAsRead(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        int count = notificationService.markMessageNotificationsAsRead(oAuth2User.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success("메시지 알림 읽음 처리 성공", count));
    }

    @Operation(
            summary = "특정 채팅방 진입 - 해당 채팅방 알림 읽음 처리",
            description = "특정 채팅방에 진입했을 때, 해당 채팅방의 메시지 알림을 읽음 처리합니다."
    )
    @ApiErrorExceptionsExample(NotificationExceptionDocs.class)
    @PostMapping(value = "/read/messages/{chatRoomId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Integer>> markChatRoomAsRead(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @Parameter(description = "채팅방 ID") @PathVariable Long chatRoomId) {

        int count = notificationService.markChatRoomAsRead(oAuth2User.getUserId(), chatRoomId);
        return ResponseEntity.ok(ApiResponseDto.success("채팅방 알림 읽음 처리 성공", count));
    }

    @Operation(
            summary = "단일 알림 읽음 처리",
            description = "특정 알림을 읽음 처리합니다."
    )
    @ApiErrorExceptionsExample(NotificationExceptionDocs.class)
    @PostMapping(value = "/read/{notificationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Long>> markAsRead(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @Parameter(description = "알림 ID") @PathVariable Long notificationId) {

        notificationService.markAsRead(oAuth2User.getUserId(), notificationId);
        return ResponseEntity.ok(ApiResponseDto.success("알림 읽음 처리 성공", notificationId));
    }

    @Operation(
            summary = "전체 알림 읽음 처리",
            description = "사용자의 모든 미읽음 알림을 읽음 처리합니다."
    )
    @ApiErrorExceptionsExample(NotificationExceptionDocs.class)
    @PostMapping(value = "/read/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<Integer>> markAllAsRead(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {

        int count = notificationService.markAllAsRead(oAuth2User.getUserId());
        return ResponseEntity.ok(ApiResponseDto.success("전체 알림 읽음 처리 성공", count));
    }
}
