package org.swyp.linkit.domain.credit.repository;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.swyp.linkit.domain.credit.entity.Credit;
import org.swyp.linkit.domain.user.entity.OAuthProvider;
import org.swyp.linkit.domain.user.entity.User;
import org.swyp.linkit.domain.user.repository.UserRepository;
import org.swyp.linkit.global.config.JpaAuditingConfig;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
class CreditRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private CreditRepository creditRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("userId로 Credit 조회")
    public void findByUserId(){
        //given
        User savedUser = userRepository.save(createUser());

        int amount = 5;
        Credit savedCredit = creditRepository.save(createCredit(savedUser, amount));
        em.flush();
        em.clear();

        //when
        Optional<Credit> foundCredit = creditRepository.findByUserId(savedUser.getId());

        //then
        assertThat(foundCredit).isPresent();
        assertThat(foundCredit.get().getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(foundCredit.get().getAmount()).isEqualTo(savedCredit.getAmount());
    }

    private User createUser() {
        return User.create(
                OAuthProvider.KAKAO,
                "kakao_1",
                "test@test.com",
                "테스터",
                "tester");
    }

    private Credit createCredit(User user, int amount) {
        return Credit.create(user, amount);
    }

}