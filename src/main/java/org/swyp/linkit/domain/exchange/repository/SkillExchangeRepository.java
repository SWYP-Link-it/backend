package org.swyp.linkit.domain.exchange.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.projection.SkillExchangeDetailQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SkillExchangeRepository extends JpaRepository<SkillExchange, Long> {

    /**
     *  date 기준 멘토의 예약 현황 조회
     *  receiverId, date, ExchangeStatus 로 SkillExchange, UserSkill Fetch Join
     */
    @Query("SELECT se FROM SkillExchange se " +
            "JOIN FETCH se.receiverSkill " +
            "WHERE se.receiver.id = :receiverId " +
            "AND se.scheduledDate = :date " +
            "AND se.exchangeStatus != :canceled ")
    List<SkillExchange> findAllByReceiverIdAndDate(@Param("receiverId") Long receiverId,
                                                   @Param("date")LocalDate date,
                                                   @Param("canceled") ExchangeStatus canceled);

    /**
     *  보낸 요청 조회
     *  requesterId, cursor 기반 페이징
     */
    @Query("SELECT new org.swyp.linkit.domain.exchange.repository.projection.SkillExchangeDetailQuery" +
            "(se.id, se.requester.id, r.id, rs.id, cr.id, r.profileImageUrl, r.nickname, se.skillName, " +
            "se.exchangeStatus, se.creditPrice, se.message, se.createdAt, se.scheduledDate, se.startTime, " +
            "se.exchangeDuration, se.isRequesterRead) " +
            "FROM SkillExchange se " +
            "JOIN se.receiver r " +
            "JOIN se.receiverSkill rs " +
            "LEFT JOIN ChatRoom cr ON " +
            "  (cr.mentor.id = r.id AND cr.mentee.id = se.requester.id) OR " +
            "  (cr.mentor.id = se.requester.id AND cr.mentee.id = r.id) " +
            "WHERE se.requester.id = :requesterId " +
            "AND (:cursorId IS NULL OR se.id < :cursorId) " +
            "ORDER BY se.id DESC")
    Slice<SkillExchangeDetailQuery> findAllByRequesterIdWithReceiver(@Param("requesterId") Long requesterId,
                                                                     @Param("cursorId") Long cursorId,
                                                                     Pageable pageable);


    /**
     *  받은 요청 조회
     *  receiverId, cursor 기반 페이징
     */
    @Query("SELECT new org.swyp.linkit.domain.exchange.repository.projection.SkillExchangeDetailQuery" +
            "(se.id, se.receiver.id, r.id, rs.id, cr.id, r.profileImageUrl, r.nickname, se.skillName, " +
            "se.exchangeStatus, se.creditPrice, se.message, se.createdAt, se.scheduledDate, se.startTime, " +
            "se.exchangeDuration, se.isReceiverRead) " +
            "FROM SkillExchange se " +
            "JOIN se.requester r " +
            "JOIN se.receiverSkill rs " +
            "LEFT JOIN ChatRoom cr ON " +
            "  (cr.mentor.id = r.id AND cr.mentee.id = se.receiver.id) OR " +
            "  (cr.mentor.id = se.receiver.id AND cr.mentee.id = r.id) " +
            "WHERE se.receiver.id = :receiverId " +
            "AND (:cursorId IS NULL OR se.id < :cursorId) " +
            "ORDER BY se.id DESC")
    Slice<SkillExchangeDetailQuery> findAllByReceiverIdWithRequester(@Param("receiverId") Long receiver,
                                                                     @Param("cursorId") Long cursorId,
                                                                     Pageable pageable);

    /**
     *  보낸 요청 알림 읽음 처리
     *  isRequesterRead 모두 true 로 update
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE SkillExchange se SET se.isRequesterRead = true " +
            "WHERE se.requester.id = :requesterId " +
            "AND se.isRequesterRead = false")
    int bulkUpdateRequesterReadStatus(@Param("requesterId") Long requesterId);

    /**
     *  받은 요청 알림 읽음 처리
     *  isReceiverRead 모두 true 로 update
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE SkillExchange se SET se.isReceiverRead = true " +
            "WHERE se.receiver.id = :receiverId " +
            "AND se.isReceiverRead = false")
    int bulkUpdateReceiverReadStatus(@Param("receiverId") Long receiverId);

    boolean existsByReceiver_IdAndIsReceiverReadFalse(Long receiverId);
    boolean existsByRequester_IdAndIsRequesterReadFalse(Long requesterId);

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
     * 거래 만료 처리해야할 목록 조회
     * requester, receiver Fetch Join
     */
    @Query("SELECT se FROM SkillExchange se " +
            "JOIN FETCH se.requester " +
            "JOIN FETCH se.receiver " +
            "WHERE se.scheduledDate < :today " +
            "AND se.exchangeStatus = :pending")
    List<SkillExchange> findAllExpiredTargets(@Param("today") LocalDate today,
                                              @Param("pending") ExchangeStatus pending);

    /**
     *  skillExchangeId로 조회 및 Receiver, Requester Fetch Join
     */
    @Query("SELECT se FROM SkillExchange se " +
            "JOIN FETCH se.receiver " +
            "JOIN FETCH se.requester " +
            "WHERE se.id = :id")
    Optional<SkillExchange> findByIdWithRequesterAndReceiver(@Param("id") Long id);
}
