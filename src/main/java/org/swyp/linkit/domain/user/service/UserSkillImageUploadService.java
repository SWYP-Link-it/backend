package org.swyp.linkit.domain.user.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.swyp.linkit.global.error.exception.SkillImageCountExceededException;
import org.swyp.linkit.global.error.exception.SkillImageFileSizeExceededException;
import org.swyp.linkit.global.error.exception.SkillImageFileTypeNotSupportedException;
import org.swyp.linkit.global.error.exception.SkillImageUploadFailedException;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSkillImageUploadService {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final int MAX_IMAGE_COUNT = 5;
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png"
    );

    private final AmazonS3 amazonS3;

    @Value("${ncp.object-storage.bucket-name}")
    private String bucketName;

    @Value("${ncp.object-storage.skill-image-path}")
    private String skillImagePath;

    // 스킬 이미지 업로드
    public String uploadSkillImage(MultipartFile file, Long userId, Long userSkillId) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 1. 고유 파일명 생성
        String fileName = generateFileName(file, userId, userSkillId);

        // 2. NCP에 업로드
        String imageUrl = uploadToNCP(file, fileName);

        log.info("이미지 업로드 성공: {}", imageUrl);
        return imageUrl;
    }

    // 이미지 개수 검증
    public void validateImageCount(int imageCount) {
        if (imageCount > MAX_IMAGE_COUNT) {
            log.warn("이미지 개수 초과: {}", imageCount);
            throw new SkillImageCountExceededException();
        }
    }

    // 이미지 파일 검증 (사전 검증용)
    public void validateImageOnly(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        // 파일 크기 체크 (2MB)
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("파일 크기 초과: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
            throw new SkillImageFileSizeExceededException();
        }

        // 파일 타입 체크 (JPG, PNG만 허용)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("지원하지 않는 파일 형식: {} ({})", file.getOriginalFilename(), contentType);
            throw new SkillImageFileTypeNotSupportedException();
        }

        log.debug("이미지 검증 통과: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
    }

    // 고유 파일명 생성
    private String generateFileName(MultipartFile file, Long userId, Long userSkillId) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return String.format("%s%d/%d/%s%s",
                skillImagePath,
                userId,
                userSkillId,
                UUID.randomUUID(),
                extension);
    }

    // NCP Object Storage에 업로드
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

            return amazonS3.getUrl(bucketName, fileName).toString();

        } catch (IOException e) {
            log.error("NCP 이미지 업로드 실패: {}", e.getMessage(), e);
            throw new SkillImageUploadFailedException();
        }
    }

    // NCP Object Storage에서 이미지 삭제
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            log.warn("삭제할 이미지 URL이 비어있습니다.");
            return;
        }

        try {
            String key = extractKeyFromUrl(imageUrl);
            amazonS3.deleteObject(bucketName, key);
            log.info("이미지 삭제 성공: key={}", key);
        } catch (Exception e) {
            log.error("NCP 이미지 삭제 실패: url={}, error={}", imageUrl, e.getMessage(), e);
        }
    }

    // URL에서 S3 객체 키 추출
    private String extractKeyFromUrl(String imageUrl) {
        URI uri = URI.create(imageUrl);
        String rawPath = uri.getRawPath();

        // 앞의 "/" 제거
        String path = rawPath.startsWith("/") ? rawPath.substring(1) : rawPath;

        // 버킷 이름 prefix 제거
        String key = removeBucketPrefix(path);

        // URL 디코딩 (공백, 한글 등 특수문자 처리)
        return URLDecoder.decode(key, StandardCharsets.UTF_8);
    }

    // Path에서 버킷 이름 prefix 제거
    private String removeBucketPrefix(String path) {
        String prefix = bucketName + "/";

        if (path.startsWith(prefix)) {
            return path.substring(prefix.length());
        }

        return path;
    }
}