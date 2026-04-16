package org.swyp.linkit.domain.exchange.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.projection.ReceivedDetailQuery;
import org.swyp.linkit.domain.exchange.repository.projection.SentDetailQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SkillExchangeRepository extends JpaRepository<SkillExchange, Long> {

    /**
     *  date 기준 멘토의 활성화 된 스킬 거래 조회
     *  receiverId, date, ExchangeStatus 로 SkillExchange Fetch Join
     */
    @Query("SELECT se FROM SkillExchange se " +
            "WHERE se.receiver.id = :receiverId " +
            "AND se.scheduledDate = :date " +
            "AND se.exchangeStatus IN :activeStatuses")
    List<SkillExchange> findAllByReceiverIdAndDate(@Param("receiverId") Long receiverId,
                                                   @Param("date")LocalDate date,
                                                   @Param("activeStatuses") List<ExchangeStatus> activeStatuses);

    /**
     *  보낸 요청 조회
     *  requesterId, cursor 기반 페이징
     */
    @Query("SELECT new org.swyp.linkit.domain.exchange.repository.projection.SentDetailQuery" +
            "(se.id, se.requester.id, r.id, se.receiverSkillId, " +
            "CASE WHEN cr1.id IS NOT NULL THEN cr1.id ELSE cr2.id END, " +
            "r.profileImageUrl, r.nickname, se.skillName, " +
            "se.exchangeStatus, se.creditPrice, se.message, se.createdAt, se.scheduledDate, se.startTime, " +
            "se.exchangeDuration, rv.id) " +
            "FROM SkillExchange se " +
            "JOIN se.receiver r " +
            "LEFT JOIN ChatRoom cr1 ON cr1.mentor.id = r.id AND cr1.mentee.id = se.requester.id " +
            "LEFT JOIN ChatRoom cr2 ON cr2.mentor.id = se.requester.id AND cr2.mentee.id = r.id " +
            "LEFT JOIN Review rv ON rv.skillExchangeId = se.id AND rv.reviewerId = se.requester.id " +
            "WHERE se.requester.id = :requesterId " +
            "AND (:cursorId IS NULL OR se.id < :cursorId) " +
            "ORDER BY se.id DESC")
    Slice<SentDetailQuery> findAllByRequesterIdWithReceiver(@Param("requesterId") Long requesterId,
                                                            @Param("cursorId") Long cursorId,
                                                            Pageable pageable);


    /**
     *  받은 요청 조회
     *  receiverId, cursor 기반 페이징
     */
    @Query("SELECT new org.swyp.linkit.domain.exchange.repository.projection.ReceivedDetailQuery" +
            "(se.id, se.receiver.id, r.id, se.receiverSkillId, " +
            "CASE WHEN cr1.id IS NOT NULL THEN cr1.id ELSE cr2.id END, " +
            "r.profileImageUrl, r.nickname, se.skillName, " +
            "se.exchangeStatus, se.creditPrice, se.message, se.createdAt, se.scheduledDate, se.startTime, " +
            "se.exchangeDuration) " +
            "FROM SkillExchange se " +
            "JOIN se.requester r " +
            "LEFT JOIN ChatRoom cr1 ON cr1.mentor.id = r.id AND cr1.mentee.id = se.receiver.id " +
            "LEFT JOIN ChatRoom cr2 ON cr2.mentor.id = se.receiver.id AND cr2.mentee.id = r.id " +
            "WHERE se.receiver.id = :receiverId " +
            "AND (:cursorId IS NULL OR se.id < :cursorId) " +
            "ORDER BY se.id DESC")
    Slice<ReceivedDetailQuery> findAllByReceiverIdWithRequester(@Param("receiverId") Long receiver,
                                                                @Param("cursorId") Long cursorId,
                                                                Pageable pageable);

    /**
     *  receiver 수락/거절
     *  SkillExchangeId로 SkillExchange 조회
     *  Receiver Fetch Join
     */
    @Query("SELECT se FROM SkillExchange se " +
            "JOIN FETCH se.receiver " +
            "WHERE se.id = :id")
    Optional<SkillExchange> findByIdWithReceiver(@Param("id") Long id);

    /**
     *  requester 취소
     *  SkillExchangeId로 SkillExchange 조회
     *  Requester Fetch Join
     */
    @Query("SELECT se FROM SkillExchange se " +
            "JOIN FETCH se.requester " +
            "WHERE se.id = :id")
    Optional<SkillExchange> findByIdWithRequester(@Param("id") Long id);

    /**
     * 거래 만료 처리해야할 Id 조회
     */
    @Query("SELECT se.id FROM SkillExchange se " +
            "WHERE se.scheduledDate <= :today " +
            "AND se.exchangeStatus = :pending")
    List<Long> findAllExpiredTargets(@Param("today") LocalDate today,
                                     @Param("pending") ExchangeStatus pending);

    /**
     * 거래 만료 처리 대상 조회
     * Requester, Receiver Fetch Join
     */
    @Query("SELECT se FROM SkillExchange se " +
            "JOIN FETCH se.requester " +
            "JOIN FETCH se.receiver " +
            "WHERE se.id = :id")
    Optional<SkillExchange> findByIdForExpire(@Param("id") Long id);

    /**
     *  skillExchangeId로 조회 및 Receiver, Requester Fetch Join
     */
    @Query("SELECT se FROM SkillExchange se " +
            "JOIN FETCH se.receiver " +
            "JOIN FETCH se.requester " +
            "WHERE se.id = :id")
    Optional<SkillExchange> findByIdWithRequesterAndReceiver(@Param("id") Long id);


    /**
     *  멘토의 해당 월에 대한 가능 날짜 중 예약 된 현황 조회
     */
    @Query("SELECT se FROM SkillExchange se " +
            "WHERE se.receiver.id = :mentorId " +
            "AND se.scheduledDate BETWEEN :startDate AND :endDate " +
            "AND se.exchangeStatus IN :statuses")
    List<SkillExchange> findAllByReceiverIdAndDateRange(
            @Param("mentorId") Long mentorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<ExchangeStatus> statuses
    );
}
