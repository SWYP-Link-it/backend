package org.swyp.linkit.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.auth.dto.PendingUserInfoDto;
import org.swyp.linkit.domain.auth.dto.request.CompleteRegistrationRequestDto;
import org.swyp.linkit.domain.auth.redis.PendingUserStorage;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserStatus;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.auth.jwt.JwtTokenProvider;
import org.swyp.linkit.global.auth.jwt.dto.JwtTokenDto;
import org.swyp.linkit.global.error.exception.DuplicateNicknameException;
import org.swyp.linkit.global.error.exception.InvalidUserStatusException;
import org.swyp.linkit.global.error.exception.SessionExpiredException;
import org.swyp.linkit.global.error.exception.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PendingUserStorage pendingUserStorage;

    @Value("${app.default-profile-image}")
    private String defaultProfileImageUrl;

    // 회원가입 완료 처리
    @Transactional
    public JwtTokenDto completeRegistration(String tempToken, CompleteRegistrationRequestDto request) {
        // 1. tempToken 검증
        jwtTokenProvider.validateTempToken(tempToken);

        // 2. sessionId 추출
        String sessionId = jwtTokenProvider.getSubjectFromToken(tempToken);

        // 3. Redis에서 PendingUserInfo 조회
        String pendingUserJson = pendingUserStorage.getPendingUser(sessionId)
                .orElseThrow(() -> new SessionExpiredException(
                        "회원가입 세션이 만료되었습니다. 다시 로그인해주세요."
                ));

        PendingUserInfoDto pendingUserInfo = PendingUserInfoDto.fromJson(pendingUserJson);

        // 4. 닉네임 중복 확인
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateNicknameException();
        }

        // 5. User 엔티티 생성
        User user = User.create(
                pendingUserInfo.getOauthProvider(),
                pendingUserInfo.getOauthId(),
                pendingUserInfo.getEmail(),
                pendingUserInfo.getName(),
                defaultProfileImageUrl,
                request.getNickname()
        );

        // 6. 회원가입 완료 처리 (PROFILE_PENDING → ACTIVE)
        user.activateAccount();

        // 7. DB에 저장
        User savedUser = userRepository.save(user);

        // 8. Redis에서 임시 데이터 삭제
        pendingUserStorage.deletePendingUser(sessionId);

        // 9. 정식 JWT 토큰 발급
        return jwtTokenProvider.generateTokenByUserId(savedUser.getId());
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