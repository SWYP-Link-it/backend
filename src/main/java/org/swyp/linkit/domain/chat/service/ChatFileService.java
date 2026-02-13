package org.swyp.linkit.domain.chat.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.linkit.global.error.exception.ChatFileSizeExceededException;
import org.swyp.linkit.global.error.exception.ChatFileTypeNotSupportedException;
import org.swyp.linkit.global.error.exception.ChatFileUploadFailedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatFileService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private final AmazonS3 amazonS3;

    @Value("${ncp.object-storage.bucket-name}")
    private String bucketName;

    @Value("${ncp.object-storage.chat-image-path}")
    private String chatImagePath;

    public String uploadChatImage(MultipartFile file, Long roomId) {
        validateFile(file);

        String fileName = generateFileName(file, roomId);
        return uploadToNCP(file, fileName);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ChatFileUploadFailedException();
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("채팅 파일 크기 초과: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
            throw new ChatFileSizeExceededException();
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("지원하지 않는 파일 형식: {} ({})", file.getOriginalFilename(), contentType);
            throw new ChatFileTypeNotSupportedException();
        }
    }

    private String generateFileName(MultipartFile file, Long roomId) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return String.format("%s%d/%s%s",
                chatImagePath,
                roomId,
                UUID.randomUUID(),
                extension);
    }

    private String uploadToNCP(MultipartFile file, String fileName) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    fileName,
                    file.getInputStream(),
                    metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead);

            amazonS3.putObject(putObjectRequest);

            String imageUrl = amazonS3.getUrl(bucketName, fileName).toString();
            log.info("채팅 이미지 업로드 성공: {}", imageUrl);
            return imageUrl;

        } catch (IOException e) {
            log.error("채팅 이미지 업로드 실패: {}", e.getMessage(), e);
            throw new ChatFileUploadFailedException();
        }
    }
}