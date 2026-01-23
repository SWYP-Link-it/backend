package org.swyp.linkit.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.auth.dto.request.CompleteRegistrationRequestDto;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserStatus;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.auth.jwt.JwtTokenProvider;
import org.swyp.linkit.global.auth.jwt.dto.JwtTokenDto;
import org.swyp.linkit.global.error.exception.DuplicateNicknameException;
import org.swyp.linkit.global.error.exception.InvalidUserStatusException;
import org.swyp.linkit.global.error.exception.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${app.default-profile-image}")
    private String defaultProfileImageUrl;

    // 회원가입 완료 처리
    @Transactional
    public JwtTokenDto completeRegistration(String tempToken, CompleteRegistrationRequestDto request) {
        // 1. tempToken 검증
        jwtTokenProvider.validateTempToken(tempToken);

        // 2. userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(tempToken);

        // 3. User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 4. 상태 확인
        if (user.getUserStatus() != UserStatus.PROFILE_PENDING) {
            throw new InvalidUserStatusException("이미 회원가입이 완료된 사용자입니다.");
        }

        // 5. 닉네임 중복 확인
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateNicknameException();
        }

        // 6. 닉네임 업데이트
        user.updateNickname(request.getNickname());

        // 7. 프로필 이미지 업데이트
        String profileImageUrl = request.getProfileImageUrl();
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            // 프로필 이미지를 입력하지 않은 경우 NCP Object Storage 기본 이미지 사용
            profileImageUrl = defaultProfileImageUrl;
        }
        user.updateProfileImage(profileImageUrl);

        // 8. 회원가입 완료 처리 (PROFILE_PENDING → ACTIVE)
        user.completeProfile();

        // 9. 정식 JWT 토큰 발급
        return jwtTokenProvider.generateTokenByUserId(userId);
    }

    // refreshToken으로 accessToken 재발급
    @Transactional(readOnly = true)
    public JwtTokenDto refreshAccessToken(String refreshToken) {
        // 1. refreshToken 검증
        jwtTokenProvider.validateToken(refreshToken);

        // 2. userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 3. User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 4. 상태 확인
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new InvalidUserStatusException("활성화된 사용자가 아닙니다.");
        }

        // 5. 정식 JWT 토큰 발급
        return jwtTokenProvider.generateTokenByUserId(userId);
    }
}