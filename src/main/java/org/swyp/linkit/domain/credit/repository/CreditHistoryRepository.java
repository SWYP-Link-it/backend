package org.swyp.linkit.domain.credit.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.credit.entity.CreditHistory;
import org.swyp.linkit.domain.credit.entity.SupplyType;

public interface CreditHistoryRepository extends JpaRepository<CreditHistory, Long> {

    /**
     *  userId, cursor, supplyType(add, use)로 cursor 기반 페이징
     *  CreditHistory, TargetUser, SkillExchange, ReceiverSkill Fetch Join
     */
    @Query("select ch FROM CreditHistory ch " +
            "left join fetch ch.targetUser " +
            "left join fetch ch.skillExchange se " +
            "left join fetch se.receiverSkill " +
            "WHERE ch.user.id = :userId " +
            "AND (:supplyType IS NULL OR ch.supplyType = :supplyType) " +
            "AND (:cursorId IS NULL OR ch.id < :cursorId)" +
            "ORDER BY ch.id DESC")
    Slice<CreditHistory> findAllByUserIdAndSupplyType(@Param("userId") Long userId,
                                                      @Param("supplyType") SupplyType supplyType,
                                                      @Param("cursorId") Long cursorId,
                                                      Pageable pageable);
}
