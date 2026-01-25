package org.swyp.linkit.global.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.auth.jwt.dto.JwtTokenDto;
import org.swyp.linkit.global.auth.oauth.CustomOAuth2User;
import org.swyp.linkit.global.error.exception.ExpiredTokenException;
import org.swyp.linkit.global.error.exception.InvalidTokenException;
import org.swyp.linkit.global.error.exception.UserNotFoundException;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final int MILLISECONDS_TO_SECONDS = 1000;

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final long tempTokenExpiration;
    private final UserRepository userRepository;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration,
            @Value("${jwt.temp-token-expiration}") long tempTokenExpiration,
            UserRepository userRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.tempTokenExpiration = tempTokenExpiration;
        this.userRepository = userRepository;
    }

    // JWT 토큰 생성 (Access Token + Refresh Token)
    public JwtTokenDto generateTokenByUserId(Long userId) {
        String accessToken = createToken(userId, accessTokenExpiration, Map.of("auth", "ROLE_USER"));
        String refreshToken = createToken(userId, refreshTokenExpiration, null);

        return JwtTokenDto.of(accessToken, refreshToken,
                accessTokenExpiration, refreshTokenExpiration);
    }

    // 임시 토큰 생성 (회원가입 대기 중인 사용자용)
    public String generateTempToken(String sessionId) {
        return createTokenWithString(sessionId, tempTokenExpiration, Map.of("type", "TEMP"));
    }

    // 공통 토큰 생성 로직 (userId 기반)
    private String createToken(Long userId, long expirationTime, Map<String, Object> claims) {
        long now = System.currentTimeMillis();

        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationTime))
                .signWith(key, Jwts.SIG.HS256);

        if (claims != null) {
            claims.forEach(builder::claim);
        }

        return builder.compact();
    }

    // 공통 토큰 생성 로직 (String subject 기반)
    private String createTokenWithString(String subject, long expirationTime, Map<String, Object> claims) {
        long now = System.currentTimeMillis();

        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationTime))
                .signWith(key, Jwts.SIG.HS256);

        if (claims != null) {
            claims.forEach(builder::claim);
        }

        return builder.compact();
    }

    // JWT 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException("만료된 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException("지원하지 않는 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("토큰이 비어있습니다.");
        }
    }

    // 임시 토큰 전용 검증
    public void validateTempToken(String token) {
        validateToken(token);

        if (!isTempToken(token)) {
            throw new InvalidTokenException("임시 토큰이 아닙니다.");
        }
    }

    // 토큰이 임시 토큰인지 확인
    public boolean isTempToken(String token) {
        Claims claims = parseClaims(token);
        return "TEMP".equals(claims.get("type"));
    }

    // JWT 토큰에서 Subject 추출
    public String getSubjectFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    // JWT 토큰에서 사용자 ID 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    // JWT 토큰에서 CustomOAuth2User 기반 인증 정보 추출
    public Authentication getAuthentication(String token) {
        Long userId = getUserIdFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        CustomOAuth2User oAuth2User = new CustomOAuth2User(
                user,
                Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "name", user.getName(),
                        "nickname", user.getNickname()
                )
        );

        return new OAuth2AuthenticationToken(
                oAuth2User,
                oAuth2User.getAuthorities(),
                user.getOauthProvider().toString().toLowerCase()
        );
    }

    // JWT 토큰 파싱
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // tempToken 쿠키 Max-Age 반환 (초 단위)
    public int getTempTokenMaxAge() {
        return (int) (tempTokenExpiration / MILLISECONDS_TO_SECONDS);
    }
}