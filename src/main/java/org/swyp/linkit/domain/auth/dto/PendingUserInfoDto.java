package org.swyp.linkit.domain.auth.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.global.error.exception.JsonSerializationException;

// 소셜 로그인 후 회원가입 대기 중인 사용자의 임시 정보
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingUserInfoDto {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private OAuthProvider oauthProvider;
    private String oauthId;
    private String email;
    private String name;
    private String profileImageUrl;

    // 객체를 JSON 문자열로 변환
    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new JsonSerializationException(
                    "PendingUserInfo JSON 변환 실패: " + e.getMessage()
            );
        }
    }

    // JSON 문자열을 객체로 변환
    public static PendingUserInfoDto fromJson(String json) {
        try {
            return objectMapper.readValue(json, PendingUserInfoDto.class);
        } catch (JsonProcessingException e) {
            throw new JsonSerializationException(
                    "PendingUserInfo JSON 파싱 실패: " + e.getMessage()
            );
        }
    }
}