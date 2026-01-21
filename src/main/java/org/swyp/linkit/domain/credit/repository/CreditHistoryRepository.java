package org.swyp.linkit.domain.credit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swyp.linkit.domain.credit.entity.CreditHistory;

public interface CreditHistoryRepository extends JpaRepository<CreditHistory, Long> {
}
