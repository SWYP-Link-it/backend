package org.swyp.linkit.domain.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.swyp.linkit.domain.chat.dto.ChatMessageDto;
import org.swyp.linkit.domain.chat.dto.ChatRoomDto;
import org.swyp.linkit.domain.chat.entity.ChatRoomStatus;
import org.swyp.linkit.domain.chat.entity.SenderRole;
import org.swyp.linkit.domain.chat.service.ChatRoomService;
import org.swyp.linkit.domain.chat.service.ChatService;
import org.swyp.linkit.global.auth.jwt.JwtTokenProvider;
import org.swyp.linkit.global.error.exception.ChatNotParticipantException;
import org.swyp.linkit.global.error.exception.ChatRoomNotFoundException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@ActiveProfiles("test")
@DisplayName("ChatController API 테스트")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @MockBean
    private ChatRoomService chatRoomService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private Long mentorId;
    private Long menteeId;
    private Long roomId;

    @BeforeEach
    void setUp() {
        mentorId = 1L;
        menteeId = 2L;
        roomId = 100L;
    }

    @Nested
    @DisplayName("채팅방 API")
    class ChatRoomApi {

        @Test
        @DisplayName("채팅방을 생성하거나 조회한다")
        @WithMockUser(username = "1")
        void createOrGetRoom() throws Exception {
            // given
            ChatRoomDto roomDto = createChatRoomDto(roomId, mentorId, menteeId);
            given(chatRoomService.createOrGetRoom(mentorId, menteeId)).willReturn(roomDto);

            // when & then
            mockMvc.perform(post("/chat/rooms")
                            .param("mentorId", mentorId.toString())
                            .param("menteeId", menteeId.toString())
                            .with(csrf())
                            .with(user("1")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.roomId").value(roomId))
                    .andExpect(jsonPath("$.data.mentorId").value(mentorId))
                    .andExpect(jsonPath("$.data.menteeId").value(menteeId));

            verify(chatRoomService).createOrGetRoom(mentorId, menteeId);
        }

        @Test
        @DisplayName("채팅방 생성 시 본인이 참여자가 아니면 실패한다")
        @WithMockUser(username = "999")
        void createOrGetRoom_notParticipant() throws Exception {
            // when & then
            mockMvc.perform(post("/chat/rooms")
                            .param("mentorId", mentorId.toString())
                            .param("menteeId", menteeId.toString())
                            .with(csrf())
                            .with(user("999")))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(chatRoomService, never()).createOrGetRoom(anyLong(), anyLong());
        }

        @Test
        @DisplayName("내 채팅방 목록을 조회한다")
        @WithMockUser(username = "1")
        void getMyRooms() throws Exception {
            // given
            ChatRoomDto room1 = createChatRoomDto(100L, mentorId, 10L);
            ChatRoomDto room2 = createChatRoomDto(101L, mentorId, 20L);
            given(chatRoomService.findRoomsByUserId(mentorId)).willReturn(List.of(room1, room2));

            // when & then
            mockMvc.perform(get("/chat/rooms")
                            .with(user("1")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));

            verify(chatRoomService).findRoomsByUserId(mentorId);
        }

        @Test
        @DisplayName("채팅방이 없으면 빈 목록을 반환한다")
        @WithMockUser(username = "1")
        void getMyRooms_empty() throws Exception {
            // given
            given(chatRoomService.findRoomsByUserId(mentorId)).willReturn(Collections.emptyList());

            // when & then
            mockMvc.perform(get("/chat/rooms")
                            .with(user("1")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("채팅방 상세 정보를 조회한다")
        @WithMockUser(username = "1")
        void getRoom() throws Exception {
            // given
            ChatRoomDto roomDto = createChatRoomDto(roomId, mentorId, menteeId);
            given(chatRoomService.isParticipant(roomId, mentorId)).willReturn(true);
            given(chatRoomService.findDtoById(roomId)).willReturn(roomDto);

            // when & then
            mockMvc.perform(get("/chat/rooms/{roomId}", roomId)
                            .with(user("1")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.roomId").value(roomId));

            verify(chatRoomService).isParticipant(roomId, mentorId);
            verify(chatRoomService).findDtoById(roomId);
        }

        @Test
        @DisplayName("참여자가 아닌 채팅방 조회 시 실패한다")
        @WithMockUser(username = "999")
        void getRoom_notParticipant() throws Exception {
            // given
            Long nonParticipantId = 999L;
            given(chatRoomService.isParticipant(roomId, nonParticipantId)).willReturn(false);

            // when & then
            mockMvc.perform(get("/chat/rooms/{roomId}", roomId)
                            .with(user("999")))
                    .andDo(print())
                    .andExpect(status().isForbidden());

            verify(chatRoomService).isParticipant(roomId, nonParticipantId);
            verify(chatRoomService, never()).findDtoById(anyLong());
        }

        @Test
        @DisplayName("채팅방 상태를 변경한다")
        @WithMockUser(username = "1")
        void updateRoomStatus() throws Exception {
            // given
            given(chatRoomService.isParticipant(roomId, mentorId)).willReturn(true);
            doNothing().when(chatRoomService).updateStatus(roomId, ChatRoomStatus.CLOSED);

            // when & then
            mockMvc.perform(patch("/chat/rooms/{roomId}/status", roomId)
                            .param("status", "CLOSED")
                            .with(csrf())
                            .with(user("1")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(chatRoomService).updateStatus(roomId, ChatRoomStatus.CLOSED);
        }

        @Test
        @DisplayName("채팅방을 삭제한다")
        @WithMockUser(username = "1")
        void deleteRooms() throws Exception {
            // given
            List<Long> roomIds = List.of(100L, 101L);
            doNothing().when(chatRoomService).deleteRooms(mentorId, roomIds);

            // when & then
            mockMvc.perform(delete("/chat/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(roomIds))
                            .with(csrf())
                            .with(user("1")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(chatRoomService).deleteRooms(mentorId, roomIds);
        }
    }

    @Nested
    @DisplayName("메시지 API")
    class MessageApi {

        @Test
        @DisplayName("채팅방 메시지 목록을 조회한다")
        @WithMockUser(username = "1")
        void getMessages() throws Exception {
            // given
            ChatMessageDto message1 = createChatMessageDto(1L, roomId, mentorId, SenderRole.MENTOR, "안녕하세요", true);
            ChatMessageDto message2 = createChatMessageDto(2L, roomId, menteeId, SenderRole.MENTEE, "반갑습니다", false);
            ChatMessageDto message3 = createChatMessageDto(3L, roomId, mentorId, SenderRole.MENTOR, "질문있어요", true);

            given(chatService.getMessages(roomId, mentorId)).willReturn(List.of(message1, message2, message3));

            // when & then
            mockMvc.perform(get("/chat/rooms/{roomId}/messages", roomId)
                            .with(user("1")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[0].messageId").value(1))
                    .andExpect(jsonPath("$.data[0].content").value("안녕하세요"))
                    .andExpect(jsonPath("$.data[0].isMine").value(true))
                    .andExpect(jsonPath("$.data[1].messageId").value(2))
                    .andExpect(jsonPath("$.data[1].content").value("반갑습니다"))
                    .andExpect(jsonPath("$.data[1].isMine").value(false));

            verify(chatService).getMessages(roomId, mentorId);
        }

        @Test
        @DisplayName("메시지가 없는 채팅방은 빈 목록을 반환한다")
        @WithMockUser(username = "1")
        void getMessages_empty() throws Exception {
            // given
            given(chatService.getMessages(roomId, mentorId)).willReturn(Collections.emptyList());

            // when & then
            mockMvc.perform(get("/chat/rooms/{roomId}/messages", roomId)
                            .with(user("1")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("참여자가 아닌 경우 메시지 조회 시 실패한다")
        @WithMockUser(username = "999")
        void getMessages_notParticipant() throws Exception {
            // given
            Long nonParticipantId = 999L;
            given(chatService.getMessages(roomId, nonParticipantId))
                    .willThrow(new ChatNotParticipantException(roomId, nonParticipantId));

            // when & then
            mockMvc.perform(get("/chat/rooms/{roomId}/messages", roomId)
                            .with(user("999")))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("메시지를 읽음 처리한다")
        @WithMockUser(username = "1")
        void markAsRead() throws Exception {
            // given
            doNothing().when(chatService).markAsRead(roomId, mentorId);

            // when & then
            mockMvc.perform(post("/chat/rooms/{roomId}/read", roomId)
                            .with(csrf())
                            .with(user("1")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(chatService).markAsRead(roomId, mentorId);
        }

        @Test
        @DisplayName("메시지를 삭제한다")
        @WithMockUser(username = "1")
        void deleteMessages() throws Exception {
            // given
            List<Long> messageIds = List.of(1L, 2L, 3L);
            doNothing().when(chatService).deleteMessages(roomId, mentorId, messageIds);

            // when & then
            mockMvc.perform(delete("/chat/rooms/{roomId}/messages", roomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(messageIds))
                            .with(csrf())
                            .with(user("1")))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(chatService).deleteMessages(roomId, mentorId, messageIds);
        }

        @Test
        @DisplayName("존재하지 않는 채팅방의 메시지 조회 시 실패한다")
        @WithMockUser(username = "1")
        void getMessages_roomNotFound() throws Exception {
            // given
            Long invalidRoomId = 9999L;
            given(chatService.getMessages(invalidRoomId, mentorId))
                    .willThrow(new ChatRoomNotFoundException(invalidRoomId));

            // when & then
            mockMvc.perform(get("/chat/rooms/{roomId}/messages", invalidRoomId)
                            .with(user("1")))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // === Helper Methods ===

    private ChatRoomDto createChatRoomDto(Long roomId, Long mentorId, Long menteeId) {
        return ChatRoomDto.builder()
                .roomId(roomId)
                .mentorId(mentorId)
                .menteeId(menteeId)
                .status(ChatRoomStatus.OPEN)
                .unreadCount(0)
                .build();
    }

    private ChatMessageDto createChatMessageDto(Long messageId, Long roomId, Long senderId,
                                                 SenderRole senderRole, String content, boolean isMine) {
        return ChatMessageDto.builder()
                .messageId(messageId)
                .roomId(roomId)
                .senderId(senderId)
                .senderRole(senderRole)
                .content(content)
                .createdAtEpochMs(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli())
                .isMine(isMine)
                .build();
    }
}