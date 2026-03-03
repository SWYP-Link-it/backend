package org.swyp.linkit.global.loadtest;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.entity.UserSkill;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.domain.user.repository.UserSkillRepository;
import org.swyp.linkit.global.auth.jwt.JwtTokenProvider;
import org.swyp.linkit.global.error.exception.UserNotFoundException;
import org.swyp.linkit.global.error.exception.UserSkillNotFoundException;

import java.util.List;
import java.util.Map;

/**
 *
 * GET /load-test/token?nickname=requester_001
 * → { "userId": 1, "accessToken": "eyJhbG..." }
 *
 * GET /load-test/mentor-info?nickname=mentor_date
 * → { "userId": 1, "skillId": 2 }
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/load-test")
public class LoadTestTokenController {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * nickname 으로 accessToken + userId 발급.
     * k6 setup() 에서 VU별 토큰 사전 발급에 사용.
     */
    @GetMapping("/token")
    public ResponseEntity<Map<String, Object>> getToken(@RequestParam String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(UserNotFoundException::new);

        String accessToken = jwtTokenProvider.generateTokenByUserId(user.getId()).getAccessToken();

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "accessToken", accessToken
        ));
    }

    /**
     * 멘토의 userId + 첫 번째 스킬의 skillId 반환.
     * k6 setup() 에서 교환 요청 대상 멘토 정보 조회에 사용.
     */
    @GetMapping("/mentor-info")
    public ResponseEntity<Map<String, Object>> getMentorInfo(@RequestParam String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(UserNotFoundException::new);

        List<UserSkill> skills = userSkillRepository.findAllSkillsByUserId(user.getId());
        if (skills.isEmpty()) {
            throw new UserSkillNotFoundException();
        }

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "skillId", skills.get(0).getId()
        ));
    }
}
