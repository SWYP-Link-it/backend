package org.swyp.linkit.global.auth.oauth;

import java.util.Map;

public class NaverOAuth2UserInfo extends OAuth2UserInfo {

    private final Map<String, Object> response;

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
        this.response = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getProviderId() {
        return String.valueOf(response.get("id"));
    }

    @Override
    public String getEmail() {
        return String.valueOf(response.get("email"));
    }

    @Override
    public String getName() {
        return String.valueOf(response.get("name"));
    }

    @Override
    public String getProfileImageUrl() {
        return String.valueOf(response.get("profile_image"));
    }
}