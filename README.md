## 🚀 Link-It - Backend

---

## 📖 목차

1. [🔎 프로젝트 소개](#-프로젝트-소개)
2. [🎯 프로젝트 기간](#-프로젝트-기간)
3. [🧑‍💻 팀원 소개 및 역할 분담](#-팀원-소개-및-역할-분담)
4. [🚀 주요 기능](#-주요-기능)
5. [⚙️ 기술 스택](#-기술-스택)
6. [📃 API 설계서](#-api-설계서)
7. [🛠 ERD](#-erd)
8. [🛠 아키텍처](#-아키텍처)
9. [❓ 기술적 의사결정](#-기술적-의사결정)
10. [📈 성능 개선](#-성능-개선)
11. [🚨 트러블 슈팅](#-트러블-슈팅)

## 🔎 프로젝트 소개
Link-It는 비용 문제로 배움의 기회를 얻기 어려운 사람들을 위해, 금전 대신 ‘시간 기반 크레딧’으로 지식을 교환할 수 있는 **스킬 거래 플랫폼**입니다.

이용자는 본인이 가진 지식이나 경험을 **스킬로 등록**하고, 다른 이용자는 해당 스킬을 **요청하여 매칭**을 진행합니다.
매칭이 성사되어 스킬 거래가 진행되면, **진행 시간 기준으로 크레딧이 자동 정산**됩니다.

단순한 지식 공유를 넘어서 배움의 진입 장벽을 낮추고 지식의 가치를 시간 단위로 환산하며 상호 신뢰 기반의 교환 구조를 만드는 것을 목표로 합니다.

## 🎯 프로젝트 기간
- 개발 기간 : 2025.12.29 ~ 2026.03

<details>
  <summary>🎇 프로젝트 실행 방법</summary>

### 1️⃣ Git Clone
  ```bash
  git clone https://github.com/SWYP-Link-it/backend.git
```

### 2️⃣ .env 파일 설정

```
DB_HOST=
DB_PORT=
DB_NAME=
DB_ROOT_PASSWORD=
DB_USER=
DB_PASSWORD=
REDIS_HOST=
REDIS_PORT=
REDIS_PASSWORD=
KAKAO_CLIENT_ID=
KAKAO_CLIENT_SECRET=
NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=
JWT_SECRET=
JWT_ACCESS_EXPIRATION=
JWT_REFRESH_EXPIRATION=
JWT_TEMP_EXPIRATION=
DEFAULT_PROFILE_IMAGE_URL=
FRONTEND_URL=
FRONTEND_PROD_URL=
BACKEND_URL=
BACKEND_PROD_URL=
COOKIE_SECURE=
COOKIE_SAME_SITE=
COOKIE_DOMAIN=
NCP_ACCESS_KEY=
NCP_SECRET_KEY=
NCP_BUCKET_NAME=

# MySQL
DB_HOST={DB HOST}
DB_NAME={DB 이름}
DB_PASSWORD={DB Password}
DB_USER={DB USER ID}

# JWT Secret Key
JWT_SECRET_KEY={JWT KEY값}

# Encryption Secret Key
ENCRYPTION_SECRET_KEY={암호화 KEY값}

# 스프링 데이터베이스 URL
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/{데이터베이스 이름}?useSSL=false&allowPublicKeyRetrieval=true
```

### 3️⃣  Docker Compose로 컨테이너 실행
```bash
docker-compose up --build -d
```    
</details>

## 🧑‍💻 팀원 소개 및 역할 분담

|                                    **김민주**                                     |     **양재영**      |       **홍정화**        |
|:------------------------------------------------------------------------------:|:----------------:|:--------------------:|
| **프로젝트 팀장 & 백엔드 & Infra** |  **백엔드 & Infra** | **백엔드 & BE 팀장** |


## 🚀 주요 기능

- 카카오 소셜 로그인 : 카카오 OAuth 2.0 기반 소셜 로그인 (중복 가입 방지 및 회원 탈퇴 처리 포함)
- 프로필 설정 : 사용자 프로필 (경력/경험, 스킬, 선호 시간대, 교환 방법) 관리
- 스킬 게시판 : 프로필 필터링 및 게시물 상세 조회
- 랭킹 시스템 : 인기 검색어 및 인기 게시물 랭킹 조회
- 크레딧 : 크레딧 내역 제공
- 스킬 거래 : 스킬 거래 신청 및 내역 제공
- 리뷰: 리뷰(평점) 작성
- 채팅 : 멘토-멘티 간의 채팅 시스템 제공
- 알림 : 채팅 및 스킬교환 알림 제공

---

## ⚙️ 기술 스택
- OS : Windows11, Ubuntu-22.04-base
- Backend : Java 21, Spring Boot 3.5.9
- Database : MySQL, Redis, Spring Data JPA
- Security : Spring Security, OAuth2.0, JWT
- Test Tool : Postman, JUnit
- DevOps : Docker, docker-compose, Git, GitHub Actions, NCP(Server, ACG), Nginx
- ETC : IntelliJ, Swagger(OpenAPI)

## 📃 API 설계서
- [🔗 Swagger(OpenAPI) LINK ](https://api.desklab.kr/swagger-ui/index.html)

## 🛠 ERD
- ![erd.drawio.png](image/erd.png)

## 🛠 아키텍처
- ![architecture.png](image/architecture.png)


## 🚨 성능 개선 및 트러블 슈팅
### 1️⃣ 유저, 스킬 평점 조회 성능 개선 - 인덱스 적용

**[Issues]**
- 마켓의 스킬 상세 조회 부하 테스트 시 평균 Latency → 2.63s로 매우 느린 응답 발생
- 쿼리 로그 분석 결과, 평점 관련 쿼리가 의심
- 평점 집계 쿼리 3개(유저 평균 평점 AVG, 스킬 평균 평점 AVG, 스킬 별점 분포 COUNT)가 매 요청마다 실행

**[Solve]**
1. EXPLAIN 분석으로 실행 계획 확인
- 3개의 쿼리 모두 type = All로 full scan 발생
- 별점 분포 COUNT 쿼리(GROUP BY rating)에서 Using temporary 추가 발생 → 임시 테이블 처리
2. 인덱스 생성
- reviewee_id 단일 인덱스 생성 → 유저 평점 AVG 조회 대상
- (reviewee_skill_id, rating) 복합 인덱스 생성 → 스킬 평점 AVG + 별점 분포 COUNT 조회 공통 적용

**[Result]**
- VU 100 기준, TPS 25 → 91 (257% 증가), 평균 Latency 2.63s → 16ms (99% 감소)
- 이전 대비 빠른 응답으로 사용자 경험성 향상
---
### 2️⃣ 스킬 조회·검색 API 집계 처리 병목 해결

**[Issues]**
- 요청마다 집계 데이터를 DB에 즉시 write → REQUIRES_NEW 트랜잭션으로 커넥션 2개 동시 점유
- InnoDB INSERT ... ON DUPLICATE KEY UPDATE의 X Lock + 커넥션 풀 고갈(max: 10)로 트래픽 증가 시 병목 발생

**[Solve]**
- DB write 제거 → Redis HINCRBY로 카운터 누적 후 5분 주기 스케줄러로 배치 flush
- HGETALL + DEL 사이 race condition을 Lua 스크립트로 원자적 실행하여 유실 방지
- 키 단위 독립 트랜잭션 적용으로 flush 실패 시 해당 키만 롤백되도록 StatFlushService 분리

**[Result]**
- 에러율 21.12% → 0%, 타임아웃 962건 → 0건으로 커넥션 풀 고갈 문제 완전 해소
- 총 처리량 4,554건 → 45,663건, 평균 응답시간 10.38s → 155ms로 10배 이상 개선

---
### 3️⃣ 미읽음 카운트 동시성 이슈 해결 및 N+1 쿼리 최적화

**[Issues]**
- unreadCount 컬럼 업데이트하는 방식으로 메시지 송수신 시마다 Update Query 발생 → DB 부하 증가
- 두 사용자가 동시에 메시지 전송 시 JPA Dirty Checking 기반 UPDATE 경쟁으로 Lost Update 동시성 이슈 발생
- 알림 테이블 기반 전환 과정에서 루프 내 개별 쿼리 호출로 N+1 발생
- 단순 카운트 컬럼 방식으로는 알림 맥락 정보가 없어 알림 탭 노출 등 기능 확장 불가

**[Solve]**
- unreadCount 컬럼을 제거하고 Notification 도메인으로 개편
- 쓰기 시의 receiver_id, ref_id 인덱스 기반의 읽기 스캔을 선택
- Fetch Join로 User 조회 Lazy Loading N+1 추가 제거
- 채팅방 진입 시 미읽음 알림 N건을 쿼리 1번에 일괄 읽음 처리

**[Result]**
- Lock 추가 없이 데이터 모델 재설계만으로 Lost Update 동시성 이슈 원천 제거, 배지 카운트와 알림 목록 간 데이터 정합성 100% 확보
- 채팅방 목록 미읽음 수 조회 쿼리 N+1번 → 1번으로 감소
- 알림 목록 및 배지 카운트가 Notification 테이블 하나로 자동 동기화