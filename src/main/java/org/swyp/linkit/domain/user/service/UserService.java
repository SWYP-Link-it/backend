package org.swyp.linkit.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.error.exception.DuplicateNicknameException;
import org.swyp.linkit.global.error.exception.SameNicknameException;
import org.swyp.linkit.global.error.exception.UserNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
}