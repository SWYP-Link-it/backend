package org.swyp.linkit.global.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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
import org.swyp.linkit.global.error.ErrorCode;
import org.swyp.linkit.global.error.exception.ExpiredTokenException;
import org.swyp.linkit.global.error.exception.InvalidTokenException;
import org.swyp.linkit.global.error.exception.base.BusinessException;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final UserRepository userRepository;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration,
            UserRepository userRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.userRepository = userRepository;
    }

    // JWT 토큰 생성
    public JwtTokenDto generateTokenByUserId(Long userId) {
        long now = System.currentTimeMillis();

        String accessToken = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("auth", "ROLE_USER")
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessTokenExpiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date(now))
                .expiration(new Date(now + refreshTokenExpiration))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        return JwtTokenDto.of(accessToken, refreshToken, accessTokenExpiration);
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

    // JWT 토큰에서 CustomOAuth2User 기반 인증 정보 추출
    public Authentication getAuthentication(String token) {
        Long userId = getUserIdFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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

    // JWT 토큰에서 사용자 ID 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
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
}