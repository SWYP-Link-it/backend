package org.swyp.linkit.global.auth.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.swyp.linkit.domain.auth.dto.PendingUserInfoDto;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

// 회원가입 대기 중인 신규 사용자의 OAuth2User 구현체
@Getter
@RequiredArgsConstructor
public class PendingOAuth2UserInfo implements OAuth2User {

    private final String sessionId;
    private final PendingUserInfoDto pendingUserInfo;
    private final Map<String, Object> attributes;

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_PENDING")
        );
    }

    @Override
    public String getName() {
        return sessionId;
    }

    // 회원가입 대기 중인 사용자인지 확인
    public boolean isPending() {
        return true;
    }
}