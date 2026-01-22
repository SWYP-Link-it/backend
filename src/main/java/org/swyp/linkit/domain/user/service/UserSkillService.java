package org.swyp.linkit.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.repository.UserSkillRepository;
import org.swyp.linkit.global.error.exception.UserSkillNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSkillService {

    private final UserSkillRepository userSkillRepository;

    // UserSkill ID로 UserProfile, User 포함하여 조회
    public UserSkill getUserSkillWithProfileAndUser(Long userSkillId) {
        return userSkillRepository.findByIdWithProfileAndUser(userSkillId)
                .orElseThrow(() ->
                        new UserSkillNotFoundException("존재하지 않는 스킬입니다")
                );
    }
}