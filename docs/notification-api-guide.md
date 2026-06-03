# 알림 탭 구현 가이드 (프론트엔드용)

> **대상:** 알림 탭 UI를 구현하는 프론트엔드 개발자  
> **작성일:** 2026-04-03  
> **브랜치:** `feature/notification/refactoring`

---

## 개요

알림은 **두 가지 채널**로 전달됩니다.

| 채널 | 목적 | 시점 |
|------|------|------|
| **WebSocket (STOMP)** | 실시간 알림 푸시 | 이벤트 발생 즉시 |
| **REST API** | 알림 목록 조회 / 읽음 처리 / 배지 카운트 | 페이지 진입 시 |

---

## 1. WebSocket 실시간 알림 수신

### 연결 방법

```
엔드포인트: /ws
프로토콜:   STOMP over WebSocket (SockJS fallback 지원)
인증:       STOMP CONNECT 헤더에 Authorization: Bearer {accessToken}
```

```javascript
// SockJS + StompJS 예시
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
  { Authorization: `Bearer ${accessToken}` },
  () => {
    // 내 알림 구독
    stompClient.subscribe(`/topic/notification.${myUserId}`, (frame) => {
      const notification = JSON.parse(frame.body);
      handleNewNotification(notification);
    });
  }
);
```

### WebSocket 알림 페이로드 (NotificationMessageDto)

```json
{
  "notificationId": 42,
  "receiverId": 10,
  "senderId": 7,
  "senderNickname": "홍길동",
  "notificationType": "REQUEST_RECEIVED",
  "refId": 15,
  "message": "홍길동님이 스킬 교환을 요청했습니다.",
  "createdAtEpochMs": 1743638400000
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `notificationId` | Long | DB에 저장된 알림 ID (읽음 처리 시 사용) |
| `receiverId` | Long | 수신자 userId |
| `senderId` | Long | 발신자 userId (`null` 가능 — 시스템 알림) |
| `senderNickname` | String | 발신자 닉네임 (`"시스템"` 가능) |
| `notificationType` | String | 아래 타입 표 참고 |
| `refId` | Long | 관련 리소스 ID (타입별 의미 다름 — 아래 표 참고) |
| `message` | String | 표시할 알림 텍스트 (서버에서 생성) |
| `createdAtEpochMs` | Long | 생성 시각 (Unix ms, UTC 기준) |

---

## 2. 알림 타입별 상세

| `notificationType` | `message` 형식 | `refId` 의미 | 클릭 시 이동 |
|--------------------|----------------|--------------|-------------|
| `REQUEST_RECEIVED` | `{발신자}님이 스킬 교환을 요청했습니다.` | skillExchangeRequestId | 받은 요청 탭 |
| `REQUEST_SENT` | `{발신자}님에게 스킬 교환 요청을 보냈습니다.` | skillExchangeRequestId | 보낸 요청 탭 |
| `SENT_REQUEST_STATUS_CHANGED` | `스킬 교환 요청 상태가 변경되었습니다.` | skillExchangeRequestId | 보낸 요청 탭 |
| `RECEIVED_REQUEST_STATUS_CHANGED` | `스킬 교환 요청 상태가 변경되었습니다.` | skillExchangeRequestId | 받은 요청 탭 |
| `CHAT_MESSAGE` | `{발신자}님에게 메시지 요청이 왔습니다.` | **chatRoomId** | 해당 채팅방 |

> `SENT_REQUEST_STATUS_CHANGED`: 수락/거절/수신자 취소/만료 시 **요청자(멘티)**에게 발송  
> `RECEIVED_REQUEST_STATUS_CHANGED`: 요청자 취소/만료 시 **수신자(멘토)**에게 발송

> `CHAT_MESSAGE`의 `refId`는 **chatRoomId**입니다. 알림 클릭 시 해당 채팅방(`/chat/rooms/{refId}`)으로 바로 이동하면 됩니다.

---

## 3. REST API 목록

Base URL: `/notifications`  
인증: 모든 API에 `Authorization: Bearer {accessToken}` 필요

### 3-1. 탭별 미읽음 배지 카운트 조회

```
GET /notifications/unread-count
```

**응답:**

```json
{
  "success": true,
  "message": "미읽음 알림 개수 조회 성공",
  "data": {
    "requestTabCount": 3,
    "receivedRequestCount": 2,
    "sentRequestCount": 1,
    "messageTabCount": 5
  }
}
```

| 필드 | 설명 | 배지 표시 위치 |
|------|------|----------------|
| `requestTabCount` | 요청 관리 탭 전체 미읽음 | 요청 관리 탭 배지 |
| `receivedRequestCount` | 받은 요청 서브탭 미읽음 | 받은 요청 탭 배지 |
| `sentRequestCount` | 보낸 요청 + 상태 변경 미읽음 | 보낸 요청 탭 배지 |
| `messageTabCount` | 채팅 메시지 알림 전체 미읽음 | 메시지 탭 배지 |

**호출 시점:** 앱 진입 시 / WebSocket으로 신규 알림 수신 시마다 갱신

---

### 3-2. 특정 채팅방의 미읽음 개수 조회

```
GET /notifications/unread-count/chat-rooms/{chatRoomId}
```

**응답:**

```json
{
  "success": true,
  "message": "채팅방 미읽음 알림 개수 조회 성공",
  "data": {
    "chatRoomId": 3,
    "unreadCount": 2
  }
}
```

**호출 시점:** 채팅방 목록(`/chat/rooms`) 조회 시, 각 채팅방 행에 미읽음 배지를 표시할 때

---

### 3-3. 알림 목록 조회 (알림 탭)

```
GET /notifications
```

**응답:**

```json
{
  "success": true,
  "message": "알림 목록 조회 성공",
  "data": {
    "notifications": [
      {
        "id": 42,
        "receiverId": 10,
        "senderId": 7,
        "notificationType": "REQUEST_RECEIVED",
        "refId": 15,
        "isRead": false,
        "createdAt": "2026-04-03T09:00:00"
      },
      {
        "id": 38,
        "receiverId": 10,
        "senderId": 5,
        "notificationType": "CHAT_MESSAGE",
        "refId": 3,
        "isRead": true,
        "createdAt": "2026-04-02T18:30:00"
      }
    ],
    "totalCount": 2,
    "unreadCount": 1
  }
}
```

> - **정렬:** 미읽음 알림 전체 (최신순) → 읽은 알림 7일 이내 (최신순)
> - `isRead: false` 항목은 강조(볼드, 배경색 등) 처리 권장
> - `notificationType`과 `refId`로 클릭 시 이동 경로 결정 (위 타입 표 참고)
> - `message` 필드는 이 응답에 포함되지 않습니다 — WebSocket 수신 시점에만 제공됩니다. 알림 탭에 표시할 텍스트는 `notificationType`과 `senderId`를 조합해 클라이언트에서 생성하거나, 별도로 사용자 닉네임 API를 호출해야 합니다.

---

## 4. 읽음 처리 API

### 페이지·탭 진입 시 자동 일괄 처리

| 진입 페이지/탭 | 호출 API | 처리 대상 |
|----------------|----------|----------|
| 요청 관리 페이지 전체 | `POST /notifications/read/requests` | `REQUEST_RECEIVED` + `REQUEST_SENT` + `REQUEST_STATUS_CHANGED` |
| 받은 요청 서브탭 | `POST /notifications/read/requests/received` | `REQUEST_RECEIVED` |
| 보낸 요청 서브탭 | `POST /notifications/read/requests/sent` | `REQUEST_SENT` + `REQUEST_STATUS_CHANGED` |
| 메시지 목록 페이지 | `POST /notifications/read/messages` | 모든 `CHAT_MESSAGE` |
| 특정 채팅방 진입 | `POST /notifications/read/messages/{chatRoomId}` | 해당 채팅방 `CHAT_MESSAGE` |

> 특정 채팅방 진입 시 WebSocket `/enter` 이벤트를 사용하면 서버가 자동 처리합니다.  
> HTTP로 채팅방에 진입하는 경로가 있다면 REST API를 직접 호출하세요.

**응답 공통 형태:**

```json
{
  "success": true,
  "message": "요청 알림 읽음 처리 성공",
  "data": 3
}
```

`data`는 읽음 처리된 알림 개수입니다.

### 단건 읽음 처리

```
POST /notifications/read/{notificationId}
```

**응답:**

```json
{
  "success": true,
  "message": "알림 읽음 처리 성공",
  "data": 42
}
```

`data`는 처리된 `notificationId`입니다.

### 전체 읽음 처리

```
POST /notifications/read/all
```

**응답:** 처리된 알림 개수 반환

---

## 5. 알림 탭 구현 흐름 요약

```
앱 초기화
  └─ GET /notifications/unread-count  →  네비게이션 배지 표시
  └─ WebSocket 구독: /topic/notification.{userId}

알림 탭 진입
  └─ GET /notifications  →  목록 렌더링
  └─ (선택) POST /notifications/read/all  →  전체 읽음 처리

알림 항목 클릭
  └─ POST /notifications/read/{notificationId}
  └─ notificationType에 따라 화면 이동:
       REQUEST_RECEIVED        → 받은 요청 탭
       REQUEST_SENT            → 보낸 요청 탭
       REQUEST_STATUS_CHANGED  → 보낸 요청 탭
       CHAT_MESSAGE            → /chat/rooms/{refId}

WebSocket 신규 알림 수신 시
  └─ 알림 목록 상단에 추가 (또는 목록 새로고침)
  └─ GET /notifications/unread-count 재호출 → 배지 갱신
  └─ Toast / 스낵바로 message 필드 노출 (선택)
```

---

## 6. 에러 응답

| HTTP 상태 | 원인 |
|-----------|------|
| `401 Unauthorized` | 인증 토큰 없음 / 만료 |
| `403 Forbidden` | 본인 알림이 아닌 읽음 처리 시도 |
| `404 Not Found` | 존재하지 않는 notificationId |
| `409 Conflict` | 이미 읽은 알림을 단건 읽음 처리 시도 |

에러 응답 형태:

```json
{
  "success": false,
  "message": "해당 알림에 대한 접근 권한이 없습니다.",
  "data": null
}
```
