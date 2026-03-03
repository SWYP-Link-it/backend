package org.swyp.linkit.domain.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.linkit.domain.chat.service.ChatFileService;
import org.swyp.linkit.domain.chat.service.ChatService;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.common.dto.ApiResponseDto;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/file")
@Tag(name = "Chat File", description = "채팅 파일 업로드 API")
public class ChatFileController {

    private final ChatFileService chatFileService;
    private final ChatService chatService;

    @Operation(summary = "채팅 이미지 업로드", description = "채팅에서 사용할 이미지를 업로드합니다. 최대 10MB, JPEG/PNG/GIF/WEBP 형식만 지원합니다.")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponseDto<String> uploadFile(
            @Parameter(description = "업로드할 이미지 파일") @RequestParam("file") MultipartFile file,
            @Parameter(description = "채팅방 ID") @RequestParam("roomId") Long roomId,
            @AuthenticationPrincipal CustomOAuth2User oAuthUser) {

        Long userId = oAuthUser.getUserId();
        chatService.assertParticipant(userId, roomId);

        String fileUrl = chatFileService.uploadChatImage(file, roomId);
        log.info("채팅 이미지 업로드: roomId={}, userId={}, url={}", roomId, userId, fileUrl);

        return ApiResponseDto.success("파일 업로드 완료", fileUrl);
    }
}
