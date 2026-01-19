package org.swyp.linkit.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserStatus;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.auth.oauth.KakaoOAuth2UserInfo;
import org.swyp.linkit.global.auth.oauth.NaverOAuth2UserInfo;
import org.swyp.linkit.global.auth.oauth.OAuth2UserInfo;
import org.swyp.linkit.global.error.exception.UnsupportedOAuthProviderException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 1. OAuth2 제공자로부터 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. OAuth 제공자 확인 (kakao, naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider oAuthProvider = OAuthProvider.valueOf(registrationId.toUpperCase());

        // 3. OAuth2 사용자 정보 처리
        return processOAuth2User(oAuthProvider, oAuth2User);
    }

    // OAuth2 사용자 정보 처리
    private OAuth2User processOAuth2User(OAuthProvider provider, OAuth2User oAuth2User) {

        // 1. OAuth 제공자별 사용자 정보 추출
        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(provider, oAuth2User.getAttributes());

        String oauthId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();
        String profileImageUrl = oAuth2UserInfo.getProfileImageUrl();

        // 2. 사용자 조회 또는 생성
        User user = userRepository.findByOauthProviderAndOauthIdAndUserStatusNot(
                        provider, oauthId, UserStatus.WITHDRAWN)
                .orElse(null);

        // 3. 신규 사용자 또는 기존 사용자 처리
        if (user == null) {
            user = User.create(provider, oauthId, email, name, profileImageUrl, email);
            user = userRepository.save(user);
        } else {
            user.updateOAuthInfo(email, name);
        }

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    // OAuth 제공자에 따라 OAuth2UserInfo 반환
    private OAuth2UserInfo getOAuth2UserInfo(
            OAuthProvider oAuthProvider,
            Map<String, Object> attributes) {

        return switch (oAuthProvider) {
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);
            case NAVER -> new NaverOAuth2UserInfo(attributes);
            default -> throw new UnsupportedOAuthProviderException("지원하지 않는 OAuth 제공자입니다: " + oAuthProvider);
        };
    }
}