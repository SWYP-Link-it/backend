package org.swyp.linkit.domain.settlement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.settlement.entity.Settlement;
import org.swyp.linkit.domain.settlement.entity.SettlementStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findBySkillExchangeId(Long skillExchangeId);

    /**
     *  정산 처리 대상 조회
     *  SkillExchange, Requester, Receiver, ReceiverSkill, ReceiverProfile Fetch Join
     */
    @Query("SELECT s FROM Settlement s " +
            "JOIN FETCH s.skillExchange se " +
            "JOIN FETCH s.receiver " +
            "JOIN FETCH se.requester " +
            "JOIN FETCH se.receiverSkill rs " +
            "JOIN FETCH rs.userProfile " +
            "WHERE s.status = :pending " +
            "AND (se.scheduledDate < :today OR (se.scheduledDate = :today AND se.endTime <= :nowTime))")
    List<Settlement> findSettleTargets(@Param("pending") SettlementStatus pending,
                                       @Param("today") LocalDate today,
                                       @Param("nowTime") LocalTime nowTime);
}
