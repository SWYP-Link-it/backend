package org.swyp.linkit.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserStatus;

@Getter
@Builder
@AllArgsConstructor
public class CurrentUserResponseDto {

    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private OAuthProvider oauthProvider;
    private UserStatus userStatus;

    public static CurrentUserResponseDto from(User user) {
        return CurrentUserResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .oauthProvider(user.getOauthProvider())
                .userStatus(user.getUserStatus())
                .build();
    }
}