package org.swyp.linkit.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.auth.dto.response.CurrentUserResponseDto;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.auth.jwt.JwtTokenProvider;
import org.swyp.linkit.global.auth.jwt.dto.JwtTokenDto;
import org.swyp.linkit.global.error.exception.UserNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    //  Refresh Token으로 새로운 Access Token 발급
    public JwtTokenDto refreshToken(String refreshToken) {

        // 1. Refresh Token 검증
        jwtTokenProvider.validateToken(refreshToken);

        // 2. Refresh Token에서 User ID 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 3. 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        // 4. 새로운 JWT 토큰 발급
        return jwtTokenProvider.generateTokenByUserId(userId);
    }

    // 현재 로그인한 사용자 정보 조회
    public CurrentUserResponseDto getCurrentUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return CurrentUserResponseDto.from(user);
    }
}