package org.swyp.linkit.domain.credit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.linkit.domain.credit.entity.Credit;

import java.util.Optional;

public interface CreditRepository extends JpaRepository<Credit, Long> {

    Optional<Credit> findByUserId(Long userId);
}
