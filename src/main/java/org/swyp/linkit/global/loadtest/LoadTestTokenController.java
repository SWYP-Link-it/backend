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
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;

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

    /**
     * counterpart 그룹의 mainSkill ID 목록 반환.
     * k6 scenario1 setup() 에서 분산 조회용 skillId 풀 구성에 사용.
     * counterpart 닉네임 prefix 로 유저를 조회하고, 각 유저의 첫 번째 스킬(mainSkill) ID 를 반환.
     */
    @GetMapping("/counterpart-skill-ids")
    public ResponseEntity<List<Long>> getCounterpartSkillIds(
            @RequestParam(defaultValue = "50") int limit) {

        List<User> counterparts = userRepository.findByNicknameStartingWithOrderByIdAsc(
                "counterpart", PageRequest.of(0, limit));

        List<Long> skillIds = counterparts.stream()
                .map(user -> {
                    List<UserSkill> skills = userSkillRepository.findAllSkillsByUserId(user.getId());
                    return skills.isEmpty() ? null : skills.get(0).getId();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.ok(skillIds);
    }

    /**
     * skill_detail_receiver 그룹의 skillId 목록 반환.
     * k6 scenario5 setup() 에서 스킬 상세 조회 대상 skillId 풀 구성에 사용.
     */
    @GetMapping("/skill-detail-skill-ids")
    public ResponseEntity<List<Long>> getSkillDetailSkillIds(
            @RequestParam(defaultValue = "1000") int limit) {

        List<User> receivers = userRepository.findByNicknameStartingWithOrderByIdAsc(
                "skill_detail_receiver", PageRequest.of(0, limit));

        List<Long> skillIds = receivers.stream()
                .map(user -> {
                    List<UserSkill> skills = userSkillRepository.findAllSkillsByUserId(user.getId());
                    return skills.isEmpty() ? null : skills.get(0).getId();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.ok(skillIds);
    }
}
