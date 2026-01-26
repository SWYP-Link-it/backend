package org.swyp.linkit.global.auth.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.swyp.linkit.domain.user.entity.User;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User, Serializable {

    private final Long userId;
    private final String email;
    private final String name;
    private final String nickname;
    private final String oauthProvider;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(Long userId, String email, String name,
                            String nickname, String oauthProvider,
                            Map<String, Object> attributes) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.oauthProvider = oauthProvider;
        this.attributes = attributes;
    }

    public static CustomOAuth2User from(User user, Map<String, Object> attributes) {
        return new CustomOAuth2User(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getNickname(),
                user.getOauthProvider().name(),
                attributes
        );
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}