package org.swyp.linkit.domain.exchange.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;

import java.time.LocalDate;
import java.util.List;

public interface SkillExchangeRepository extends JpaRepository<SkillExchange, Long> {

    /**
     *  receiverId, date, ExchangeStatus 로 SkillExchange, UserSkill Fetch Join
     *  비관적 락 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT se FROM SkillExchange se " +
            "JOIN FETCH se.receiverSkill " +
            "WHERE se.receiver.id = :receiverId " +
            "AND se.scheduledDate = :date " +
            "AND se.exchangeStatus != :canceled ")
    List<SkillExchange> findAllByReceiverIdAndDate(@Param("receiverId") Long receiverId,
                                                   @Param("date")LocalDate date,
                                                   @Param("canceled") ExchangeStatus canceled);
}
