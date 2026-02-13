package org.swyp.linkit.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserProfile;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.entity.UserSkillImage;
import org.swyp.linkit.domain.user.entity.UserStatus;
import org.swyp.linkit.domain.user.entity.UserWithdrawalHistory;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.domain.user.repository.UserWithdrawalHistoryRepository;
import org.swyp.linkit.global.error.exception.AlreadyWithdrawnException;
import org.swyp.linkit.global.error.exception.DuplicateNicknameException;
import org.swyp.linkit.global.error.exception.SameNicknameException;
import org.swyp.linkit.global.error.exception.UserNotFoundException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSkillImageUploadService imageUploadService;
    private final UserWithdrawalHistoryRepository withdrawalHistoryRepository;

    // 사용자 조회
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("존재하지 않는 사용자입니다")
                );
    }

    // 닉네임 변경
    @Transactional
    public void updateNickname(Long userId, String nickname) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        String oldNickname = user.getNickname();

        // 2. 동일 닉네임이면 변경 없이 종료
        if (oldNickname.equals(nickname)) {
            throw new SameNicknameException("기존 닉네임과 동일합니다.");
        }

        // 3. 닉네임 중복 검사 (본인 제외)
        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }

        // 4. 닉네임 변경
        user.updateNickname(nickname);

        log.info("닉네임 변경: userId={}, oldNickname={}, newNickname={}",
                userId, oldNickname, nickname);
    }

    // 회원 탈퇴
    @Transactional
    public void withdrawUser(Long userId) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 2. 이미 탈퇴한 사용자 체크
        if (user.getUserStatus() == UserStatus.WITHDRAWN) {
            throw new AlreadyWithdrawnException("이미 탈퇴한 사용자입니다.");
        }

        // 3. 이미지 파일 삭제
        deleteImagesFromStorage(user);

        // 4. 탈퇴 이력 저장
        UserWithdrawalHistory withdrawalHistory = UserWithdrawalHistory.create(user);
        withdrawalHistoryRepository.save(withdrawalHistory);

        log.info("탈퇴 이력 저장: userId={}, oauthProvider={}, oauthId={}",
                userId, user.getOauthProvider(), user.getOauthId());

        // 5. User 상태 변경
        user.withdraw();

        log.info("회원 탈퇴 완료: userId={}, nickname={}", userId, user.getNickname());
    }

    // 이미지 파일 삭제
    private void deleteImagesFromStorage(User user) {
        UserProfile userProfile = user.getUserProfile();

        if (userProfile == null) {
            log.info("프로필이 없는 사용자 탈퇴: userId={}", user.getId());
            return;
        }

        // NCP Object Storage에서 이미지 파일만 삭제
        List<UserSkill> userSkills = userProfile.getUserSkills();
        for (UserSkill userSkill : userSkills) {
            List<String> imageUrls = userSkill.getImages().stream()
                    .map(UserSkillImage::getImageUrl)
                    .toList();

            for (String imageUrl : imageUrls) {
                imageUploadService.deleteImage(imageUrl);
            }

            log.info("스킬 이미지 삭제: userId={}, skillId={}, imageCount={}",
                    user.getId(), userSkill.getId(), imageUrls.size());
        }

        log.info("이미지 파일 삭제 완료: userId={}, skillCount={}",
                user.getId(), userSkills.size());
    }
}