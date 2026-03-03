package org.swyp.linkit.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {

    private final boolean secure;
    private final String sameSite;
    private final String domain;

    public CookieUtil(
            @Value("${app.cookie.secure}") boolean secure,
            @Value("${app.cookie.same-site}") String sameSite,
            @Value("${app.cookie.domain}") String domain
    ) {
        this.secure = secure;
        this.sameSite = sameSite;
        this.domain = domain;
    }

    public void addCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(maxAgeSeconds)
                .domain(domain)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0)
                .domain(domain)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
