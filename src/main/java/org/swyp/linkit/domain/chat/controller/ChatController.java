package org.swyp.linkit.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.swyp.linkit.domain.chat.dto.ChatMessageDto;
import org.swyp.linkit.domain.chat.dto.ChatRoomDto;
import org.swyp.linkit.domain.chat.dto.response.ChatMessageResponseDto;
import org.swyp.linkit.domain.chat.dto.response.ChatRoomResponseDto;
import org.swyp.linkit.domain.chat.entity.ChatRoomStatus;
import org.swyp.linkit.domain.chat.service.ChatRoomService;
import org.swyp.linkit.domain.chat.service.ChatService;
import org.swyp.linkit.global.common.dto.ApiResponse;
import org.swyp.linkit.global.error.exception.ChatNotParticipantException;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Tag(name = "Chat", description = "채팅 API")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final ChatService chatService;

    // ==================== 채팅방 API ====================

    @Operation(summary = "채팅방 생성/조회", description = "1:1 채팅방을 생성하거나 기존 채팅방을 조회합니다. 멘토와 멘티 간의 채팅방이 이미 존재하면 해당 채팅방을 반환합니다.")
    @PostMapping("/rooms")
    public ApiResponse<ChatRoomResponseDto> createOrGetRoom(
            @Parameter(description = "멘토 사용자 ID") @RequestParam Long mentorId,
            @Parameter(description = "멘티 사용자 ID") @RequestParam Long menteeId,
            Principal principal) {
        Long me = Long.parseLong(principal.getName());

        // 요청자가 멘토 또는 멘티인지 확인
        if (!me.equals(mentorId) && !me.equals(menteeId)) {
            throw new ChatNotParticipantException();
        }

        ChatRoomDto roomDto = chatRoomService.createOrGetRoom(mentorId, menteeId);
        return ApiResponse.success("채팅방 조회/생성 완료", ChatRoomResponseDto.from(roomDto));
    }

    @Operation(summary = "내 채팅방 목록 조회", description = "현재 사용자의 채팅방 목록을 조회합니다. 삭제된 채팅방은 제외되며, 마지막 메시지 기준 최신순으로 정렬됩니다.")
    @GetMapping("/rooms")
    public ApiResponse<List<ChatRoomResponseDto>> getMyRooms(Principal principal) {
        Long me = Long.parseLong(principal.getName());
        List<ChatRoomDto> roomDtos = chatRoomService.findRoomsByUserId(me);
        List<ChatRoomResponseDto> rooms = roomDtos.stream()
                .map(ChatRoomResponseDto::from)
                .collect(Collectors.toList());
        return ApiResponse.success("채팅방 목록 조회 완료", rooms);
    }

    @Operation(summary = "채팅방 상세 조회", description = "특정 채팅방의 상세 정보를 조회합니다. 채팅방 참여자만 조회할 수 있습니다.")
    @GetMapping("/rooms/{roomId}")
    public ApiResponse<ChatRoomResponseDto> getRoom(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            Principal principal) {
        Long me = Long.parseLong(principal.getName());

        // 참여자 확인
        if (!chatRoomService.isParticipant(roomId, me)) {
            throw new ChatNotParticipantException(roomId, me);
        }

        ChatRoomDto roomDto = chatRoomService.findDtoById(roomId);
        return ApiResponse.success("채팅방 상세 조회 완료", ChatRoomResponseDto.from(roomDto));
    }

    @Operation(summary = "채팅방 상태 변경", description = "채팅방의 상태를 변경합니다. 채팅방 참여자만 상태를 변경할 수 있습니다.")
    @PatchMapping("/rooms/{roomId}/status")
    public ApiResponse<Void> updateRoomStatus(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            @Parameter(description = "변경할 상태 (ACTIVE, CLOSED 등)") @RequestParam ChatRoomStatus status,
            Principal principal) {
        Long me = Long.parseLong(principal.getName());

        // 참여자 확인
        if (!chatRoomService.isParticipant(roomId, me)) {
            throw new ChatNotParticipantException(roomId, me);
        }

        chatRoomService.updateStatus(roomId, status);
        return ApiResponse.success("채팅방 상태 변경 완료", null);
    }

    @Operation(summary = "채팅방 삭제", description = "선택한 채팅방을 삭제합니다. 본인 기준으로만 삭제되며, 복수 선택이 가능합니다.")
    @DeleteMapping("/rooms")
    public ApiResponse<Void> deleteRooms(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "삭제할 채팅방 ID 목록")
            @RequestBody List<Long> roomIds,
            Principal principal) {
        Long me = Long.parseLong(principal.getName());
        chatRoomService.deleteRooms(me, roomIds);
        return ApiResponse.success("채팅방 삭제 완료", null);
    }

    // ==================== 메시지 API ====================

    @Operation(summary = "메시지 목록 조회", description = "채팅방의 메시지 목록을 조회합니다. 본인이 삭제한 메시지는 제외됩니다.")
    @GetMapping("/rooms/{roomId}/messages")
    public ApiResponse<List<ChatMessageResponseDto>> getMessages(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            Principal principal) {
        Long me = Long.parseLong(principal.getName());
        List<ChatMessageDto> messageDtos = chatService.getMessages(roomId, me);
        List<ChatMessageResponseDto> messages = messageDtos.stream()
                .map(ChatMessageResponseDto::from)
                .collect(Collectors.toList());
        return ApiResponse.success("메시지 목록 조회 완료", messages);
    }

    @Operation(summary = "메시지 읽음 처리", description = "채팅방의 모든 메시지를 읽음 처리합니다.")
    @PostMapping("/rooms/{roomId}/read")
    public ApiResponse<Void> markAsRead(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            Principal principal) {
        Long me = Long.parseLong(principal.getName());
        chatService.markAsRead(roomId, me);
        return ApiResponse.success("읽음 처리 완료", null);
    }

    @Operation(summary = "메시지 삭제", description = "선택한 메시지를 삭제합니다. 본인 기준으로만 삭제되며, 복수 선택이 가능합니다.")
    @DeleteMapping("/rooms/{roomId}/messages")
    public ApiResponse<Void> deleteMessages(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "삭제할 메시지 ID 목록")
            @RequestBody List<Long> messageIds,
            Principal principal) {
        Long me = Long.parseLong(principal.getName());
        chatService.deleteMessages(roomId, me, messageIds);
        return ApiResponse.success("메시지 삭제 완료", null);
    }
}