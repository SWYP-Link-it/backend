package org.swyp.linkit.domain.exchange.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.swyp.linkit.domain.chat.entity.ChatRoom;
import org.swyp.linkit.domain.exchange.entity.ExchangeStatus;
import org.swyp.linkit.domain.exchange.entity.SkillExchange;
import org.swyp.linkit.domain.exchange.repository.projection.SkillExchangeDetailQuery;
import org.swyp.linkit.domain.user.entity.*;
import org.swyp.linkit.global.config.JpaAuditingConfig;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@DisplayName("SkillExchangeRepository 단위 테스트")
class SkillExchangeRepositoryTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    SkillExchangeRepository exchangeRepository;

    private User receiver;
    private User requester;
    private UserSkill receiverSkill;
    private LocalDate scheduledDate;
    private List<SkillExchange> exchangeList = new ArrayList<>();
    private ChatRoom chatRoom;

    @BeforeEach
    void setup() {
        testSetup();
    }

    @Test
    @DisplayName("receiverId, date, exchangeStatus 로 SkillExchangeList 조회")
    public void findAllByReceiverIdAndDate() {
        //given
        Long receiverId = receiver.getId();
        ExchangeStatus status = ExchangeStatus.CANCELED;

        //when
        List<SkillExchange> sut = exchangeRepository.findAllByReceiverIdAndDate(receiverId, scheduledDate, status);

        //then
        assertThat(sut.size()).isEqualTo(10);

        boolean hasCanceled = sut.stream().anyMatch(se -> se.getExchangeStatus() == status);
        assertThat(hasCanceled).isFalse();

        SkillExchange firstExchange = sut.get(0);
        assertEquals(receiverId, firstExchange.getReceiver().getId());
        assertEquals(scheduledDate, firstExchange.getScheduledDate());
        assertNotNull(firstExchange.getReceiverSkill().getSkillName());
    }

    @Nested
    @DisplayName("스킬 거래 보낸 요청 페이징 조회 (findAllByRequesterIdWithReceiver)")
    class FindAllByRequesterIdWithReceiver {

        @Test
        @DisplayName("cursor Null")
        public void findAllByRequesterIdWithReceiver_cursor_null() {
            // given
            Long requesterId = requester.getId();
            Long cursorId = null;
            int size = 5;
            Pageable pageable = PageRequest.of(0, size);

            // when
            Slice<SkillExchangeDetailQuery> sut = exchangeRepository
                    .findAllByRequesterIdWithReceiver(requesterId, cursorId, pageable);

            // then
            assertThat(sut.getContent().size()).isEqualTo(size);
            assertThat(sut.hasNext()).isTrue();

            //페이징 조회 목록 검증
            int listSize = exchangeList.size();
            for (int i = 0; i < size; i++) {
                SkillExchange se = exchangeList.get(listSize - 1 - i);
                assertAsRequesterQueryDto(sut, i, se, requesterId);
            }
        }

        @Test
        @DisplayName("cursor NotNull")
        public void findAllByRequesterIdWithReceiver_cursor_not_null() {
            // given
            Long requesterId = requester.getId();
            Long cursorId = exchangeList.get(3).getId();
            int size = 5;
            Pageable pageable = PageRequest.of(0, size);

            // when
            Slice<SkillExchangeDetailQuery> sut = exchangeRepository
                    .findAllByRequesterIdWithReceiver(requesterId, cursorId, pageable);

            // then
            assertThat(sut.getContent().size()).isEqualTo(3);
            // 마지막 값 조회로 인해 false
            assertThat(sut.hasNext()).isFalse();

            //페이징 조회 목록 검증
            List<SkillExchangeDetailQuery> content = sut.getContent();
            for (int i = 0; i < content.size(); i++) {
                SkillExchange se = exchangeList.get(2 - i);
                assertAsRequesterQueryDto(sut, i, se, requesterId);
            }
        }

        @Test
        @DisplayName("cursor NotNull, chatRoom 존재하지 않을때 null 확인")
        public void findAllByRequesterIdWithReceiver_cursor_not_null_chatRoom_null() {
            // given
            Long requesterId = requester.getId();
            Long cursorId = exchangeList.get(3).getId();
            int size = 5;
            Pageable pageable = PageRequest.of(0, size);
            // chatRoom 삭제
            ChatRoom savedChatRoom = em.find(ChatRoom.class, chatRoom.getId());
            em.remove(savedChatRoom);
            em.flush();
            em.clear();

            // when
            Slice<SkillExchangeDetailQuery> sut = exchangeRepository
                    .findAllByRequesterIdWithReceiver(requesterId, cursorId, pageable);

            // then
            assertThat(sut.getContent().size()).isEqualTo(3);
            // 마지막 값 조회로 인해 false
            assertThat(sut.hasNext()).isFalse();
            //페이징 조회 목록 검증
            List<SkillExchangeDetailQuery> content = sut.getContent();
            for (SkillExchangeDetailQuery queryDto : content) {
                // chatRoomId = Null 검증
                assertThat(queryDto.chatRoomId()).isNull();
            }
        }
    }

    @Nested
    @DisplayName("스킬 거래 받음 요청 페이징 조회 (findAllByReceiverIdWithRequester)")
    class FindAllByReceiverIdWithRequester {

        @Test
        @DisplayName("cursor Null")
        public void findAllByReceiverIdWithRequester_cursor_null() {
            // given
            Long receiverId = receiver.getId();
            Long cursorId = null;
            int size = 5;
            Pageable pageable = PageRequest.of(0, size);

            // when
            Slice<SkillExchangeDetailQuery> sut = exchangeRepository
                    .findAllByReceiverIdWithRequester(receiverId, cursorId, pageable);

            // then
            assertThat(sut.getContent().size()).isEqualTo(size);
            assertThat(sut.hasNext()).isTrue();

            //페이징 조회 목록 검증
            int listSize = exchangeList.size();
            for (int i = 0; i < size; i++) {
                SkillExchange se = exchangeList.get(listSize - 1 - i);
                assertAsReceiverQueryDto(sut, i, se, receiverId);
            }
        }

        @Test
        @DisplayName("cursor NotNull")
        public void findAllByReceiverIdWithRequester_cursor_not_null() {
            // given
            Long receiverId = receiver.getId();
            Long cursorId = exchangeList.get(3).getId();
            int size = 5;
            Pageable pageable = PageRequest.of(0, size);

            // when
            Slice<SkillExchangeDetailQuery> sut = exchangeRepository
                    .findAllByReceiverIdWithRequester(receiverId, cursorId, pageable);

            // then
            assertThat(sut.getContent().size()).isEqualTo(3);
            // 마지막 값 조회로 인해 false
            assertThat(sut.hasNext()).isFalse();

            //페이징 조회 목록 검증
            List<SkillExchangeDetailQuery> content = sut.getContent();
            for (int i = 0; i < content.size(); i++) {
                SkillExchange se = exchangeList.get(2 - i);
                assertAsReceiverQueryDto(sut, i, se, receiverId);
            }
        }
    }

    @Test
    @DisplayName("보낸 요청 알림 읽음 처리 bulkUpdate")
    public void bulkUpdateRequesterReadStatus() {
        // given
        Long requesterId = requester.getId();
        // setup에서 생성된 exchangeList는 모두 isRequesterRead = false
        long totalCount = exchangeList.size();

        // when
        int updatedCount = exchangeRepository.bulkUpdateRequesterReadStatus(requesterId);

        // then
        // update된 개수 검증
        assertThat(updatedCount).isEqualTo((int) totalCount);

        // 영속성 컨텍스트 clear로 인한 재조회
        List<SkillExchange> results = getRequesterBulkUpdateResults(requesterId);
        assertThat(results).allMatch(SkillExchange::isRequesterRead);
    }

    @Test
    @DisplayName("받은 요청 알림 읽음 처리 bulkUpdate")
    public void bulkUpdateReceiverReadStatus() {
        // given
        Long receiverId = receiver.getId();
        // setup에서 생성된 exchangeList는 모두 isRequesterRead = false
        long totalCount = exchangeList.size();

        // when
        System.out.print("== bulkUpdate ==");
        int updatedCount = exchangeRepository.bulkUpdateReceiverReadStatus(receiverId);

        // then
        // update된 개수 검증
        assertThat(updatedCount).isEqualTo((int) totalCount);

        // 영속성 컨텍스트 clear로 인한 재조회
        List<SkillExchange> results = getReceiverBulkUpdateResults(receiverId);
        assertThat(results).allMatch(SkillExchange::isReceiverRead);
    }

    @Test
    @DisplayName("receiverId로 isReceiverRead = false 조회")
    public void existsByReceiver_IdAndIsReceiverReadFalse() {
        // given
        Long receiverId = receiver.getId();

        // when
        boolean sut = exchangeRepository.existsByReceiver_IdAndIsReceiverReadFalse(receiverId);

        // then
        assertThat(sut).isTrue();
    }

    @Test
    @DisplayName("requesterId로 isRequesterRead = false 조회")
    public void existsByRequester_IdAndIsRequesterReadFalse() {
        // given
        Long requesterId = requester.getId();

        // when
        boolean sut = exchangeRepository.existsByRequester_IdAndIsRequesterReadFalse(requesterId);

        // then
        assertThat(sut).isTrue();
    }

    @Test
    @DisplayName("SkillExchangeId로 SkillExchange 조회")
    public void findByIdWithReceiver() {
        // given
        SkillExchange skillExchange = exchangeList.get(0);
        Long skillExchangeId = skillExchange.getId();

        // when
        Optional<SkillExchange> optionalSkillExchange = exchangeRepository.findByIdWithReceiver(skillExchangeId);

        // then
        assertThat(optionalSkillExchange).isPresent();
        SkillExchange sut = optionalSkillExchange.get();

        assertThat(sut.getId()).isEqualTo(skillExchangeId);
        assertThat(sut.getReceiver().getId()).isEqualTo(receiver.getId());
    }

    @Test
    @DisplayName("SkillExchangeId로 SkillExchange 조회")
    public void findByIdWithRequester() {
        // given
        SkillExchange skillExchange = exchangeList.get(0);
        Long skillExchangeId = skillExchange.getId();

        // when
        Optional<SkillExchange> optionalSkillExchange = exchangeRepository.findByIdWithRequester(skillExchangeId);

        // then
        assertThat(optionalSkillExchange).isPresent();
        SkillExchange sut = optionalSkillExchange.get();

        assertThat(sut.getId()).isEqualTo(skillExchangeId);
        assertThat(sut.getRequester().getId()).isEqualTo(requester.getId());
    }

    @Test
    @DisplayName("거래 만료 처리해야할 목록 조회")
    public void findAllExpiredTargets() {
        // given
        LocalDate today = LocalDate.now();
        ExchangeStatus pendingStatus = ExchangeStatus.PENDING;

        // 대상 데이터 탐색
        List<SkillExchange> expectedTargets = exchangeRepository.findAll().stream()
                .filter(se -> se.getExchangeStatus().equals(pendingStatus))
                .filter(se -> se.getScheduledDate().isBefore(today))
                .toList();

        int expectedSize = expectedTargets.size();
        assertThat(expectedSize).isEqualTo(10);

        // when
        System.out.println("== 조회 쿼리 시작 ==");
        List<SkillExchange> expiredTargets = exchangeRepository.findAllExpiredTargets(today, pendingStatus);

        // then
        assertThat(expiredTargets.size()).isEqualTo(expectedSize);
        for (SkillExchange target : expiredTargets) {
            // 조회된 데이터가 모두 today 이전
            assertThat(target.getScheduledDate()).isBefore(today);
            // 조회된 데이터가 모두 pending
            assertThat(target.getExchangeStatus()).isEqualTo(pendingStatus);
        }
    }

    private List<SkillExchange> getReceiverBulkUpdateResults(Long receiverId) {
        return em.getEntityManager()
                .createQuery("select se from SkillExchange se where se.receiver.id = :receiverId", SkillExchange.class)
                .setParameter("receiverId", receiverId)
                .getResultList();
    }

    private List<SkillExchange> getRequesterBulkUpdateResults(Long requesterId) {
        return em.getEntityManager()
                .createQuery("select se from SkillExchange se where se.requester.id = :requesterId", SkillExchange.class)
                .setParameter("requesterId", requesterId)
                .getResultList();
    }

    private void assertAsRequesterQueryDto(Slice<SkillExchangeDetailQuery> sut, int i, SkillExchange se, Long requesterId) {
        SkillExchangeDetailQuery queryDto = sut.getContent().get(i);
        assertThat(queryDto.skillExchangeId()).isEqualTo(se.getId());
        assertThat(queryDto.userId()).isEqualTo(requesterId);
        assertThat(queryDto.targetUserId()).isEqualTo(receiver.getId());
        assertThat(queryDto.skillId()).isEqualTo(receiverSkill.getId());
        assertThat(queryDto.chatRoomId()).isEqualTo(chatRoom.getId());
        assertThat(queryDto.targetProfileImageUrl()).isEqualTo(receiver.getProfileImageUrl());
        assertThat(queryDto.targetNickname()).isEqualTo(receiver.getNickname());
        assertThat(queryDto.skillName()).isEqualTo(receiverSkill.getSkillName());
        assertThat(queryDto.exchangeStatus()).isEqualTo(se.getExchangeStatus());
        assertThat(queryDto.creditPrice()).isEqualTo(se.getCreditPrice());
        assertThat(queryDto.message()).isEqualTo(se.getMessage());
        assertThat(queryDto.createdAt()).isEqualToIgnoringNanos(se.getCreatedAt());
        assertThat(queryDto.exchangeDate()).isEqualTo(scheduledDate);
        assertThat(queryDto.exchangeTime()).isEqualTo(se.getStartTime());
        assertThat(queryDto.exchangeDuration()).isEqualTo(se.getExchangeDuration());
        assertThat(queryDto.isRead()).isFalse();
    }

    private void assertAsReceiverQueryDto(Slice<SkillExchangeDetailQuery> sut, int i, SkillExchange se, Long receverId) {
        SkillExchangeDetailQuery queryDto = sut.getContent().get(i);
        assertThat(queryDto.skillExchangeId()).isEqualTo(se.getId());
        assertThat(queryDto.userId()).isEqualTo(receverId);
        assertThat(queryDto.targetUserId()).isEqualTo(requester.getId());
        assertThat(queryDto.skillId()).isEqualTo(receiverSkill.getId());
        assertThat(queryDto.chatRoomId()).isEqualTo(chatRoom.getId());
        assertThat(queryDto.targetProfileImageUrl()).isEqualTo(requester.getProfileImageUrl());
        assertThat(queryDto.targetNickname()).isEqualTo(requester.getNickname());
        assertThat(queryDto.skillName()).isEqualTo(receiverSkill.getSkillName());
        assertThat(queryDto.exchangeStatus()).isEqualTo(se.getExchangeStatus());
        assertThat(queryDto.creditPrice()).isEqualTo(se.getCreditPrice());
        assertThat(queryDto.message()).isEqualTo(se.getMessage());
        assertThat(queryDto.createdAt()).isEqualToIgnoringNanos(se.getCreatedAt());
        assertThat(queryDto.exchangeDate()).isEqualTo(scheduledDate);
        assertThat(queryDto.exchangeTime()).isEqualTo(se.getStartTime());
        assertThat(queryDto.exchangeDuration()).isEqualTo(se.getExchangeDuration());
        assertThat(queryDto.isRead()).isFalse();
    }

    private void testSetup() {
        // requester, receiver 세팅
        requester = createUser();
        receiver = createUser();
        em.persist(requester);
        em.persist(receiver);

        // chatRoom 세팅
        chatRoom = createChatRoom(requester.getId(), receiver.getId());
        em.persist(chatRoom);

        // userSkill 세팅
        SkillCategory skillCategory1 = createSkillCategory(SkillCategoryType.DEVELOPMENT);
        SkillCategory skillCategory2 = createSkillCategory(SkillCategoryType.DESIGN);
        em.persist(skillCategory1);
        em.persist(skillCategory2);

        UserSkill requesterSkill = createUserSkill(skillCategory1);
        receiverSkill = createUserSkill(skillCategory2);

        UserProfile requesterProfile = createProfile(requester, requesterSkill);
        UserProfile receiverProfile = createProfile(receiver, receiverSkill);
        em.persist(requesterProfile);
        em.persist(receiverProfile);
        em.persist(requesterSkill);
        em.persist(receiverSkill);

        // skillExchange 세팅
        // 짝수면 PENDING, 홀수면 CANCELED
        scheduledDate = LocalDate.now().minusDays(1);
        for (int i = 1; i <= 20; i++) {
            SkillExchange exchange = createExchange(requester, receiver, receiverSkill,
                    scheduledDate, LocalTime.of(i, 0), LocalTime.of(i + 1, 0));
            if (i % 2 == 1) exchange.updateExchangeStatus(ExchangeStatus.CANCELED);
            SkillExchange savedSkillExchange = exchangeRepository.save(exchange);
            exchangeList.add(savedSkillExchange);
        }

        em.flush();
        em.clear();
    }

    private ChatRoom createChatRoom(Long mentorId, Long menteeId) {
        return ChatRoom.create(mentorId, menteeId);
    }

    private User createUser() {
        String uuid = UUID.randomUUID().toString();
        return User.create(
                OAuthProvider.KAKAO,
                "kakao" + uuid,
                uuid + "email@example.com",
                "name" + uuid,
                "https://image",
                "nickname" + uuid);
    }

    private UserProfile createProfile(User user, UserSkill userSkill) {
        UserProfile userProfile = UserProfile.create(
                user,
                "description",
                ExchangeType.OFFLINE,
                null,
                null);
        userProfile.addUserSkill(userSkill);
        return userProfile;
    }

    private SkillCategory createSkillCategory(SkillCategoryType type) {
        return SkillCategory.create(type);
    }

    private UserSkill createUserSkill(SkillCategory type) {
        return UserSkill.create(
                type,
                "skillName",
                "skillTitle",
                SkillProficiency.HIGH,
                "description",
                30);
    }

    private SkillExchange createExchange(User requester, User receiver, UserSkill receiverSkill,
                                         LocalDate scheduledDate, LocalTime start, LocalTime end) {
        return SkillExchange.create(
                requester,
                receiver,
                receiverSkill,
                scheduledDate,
                start,
                end,
                "defaultMessage");
    }


}