package org.swyp.linkit.global.auth.oauth;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public abstract class OAuth2UserInfo {

    protected final Map<String, Object> attributes;

    public abstract String getProviderId();
    public abstract String getEmail();
    public abstract String getName();
    public abstract String getProfileImageUrl();
}