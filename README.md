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
- 개발 기간 : 2025.12.29 ~ 2026.02.20 (진행 중)

<details>
  <summary>🎇 프로젝트 실행 방법</summary>

### 1️⃣ Git Clone
  ```bash
  git clone 
```

### 2️⃣ .env 파일 설정

```
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

### 3️⃣ Docker 이미지 빌드
``` bash
```

### 4️⃣ Docker Compose로 컨테이너 실행
```bash
docker-compose up --build -d
```    
</details>

## 🧑‍💻 팀원 소개 및 역할 분담

|                                    **김민주**                                     |                               **양재영**                                |                                  **홍정화**                                   |
|:------------------------------------------------------------------------------:|:--------------------------------------------------------------------:|:--------------------------------------------------------------------------:|
| <img src="image/김민주.jpg" height=150 width=150> <br/> **프로젝트 팀장 & 백엔드 & Infra** | <img src="image/양재영.jpg" height=150 width=150> <br/> **백엔드 & Infra** | <img src="image/홍정화.jpg" height=150 width=150> <br/> **백엔드 & 프로젝트 백엔드 팀장** |


## 🚀 주요 기능

### 📌 사용자
- 기능 설명 
- 기능 설명
- 기능 설명

### 📌 스킬 등록
- 이용자가 본인이 보유한 지식·경험을 스킬로 등록하여 스킬 장터에 노출합니다.
- 스킬명, 스킬 소개, 경력 및 경험, 교환 방식 (1:1, 온라인 등), 가능 시간 (요일/시간대), 1회 진행 기준 시간 (예: 30분 / 60분) 입력
- 등록된 스킬은 다른 이용자가 탐색 및 요청 가능

### 📌 스킬 요청
- 스킬 장터에 등록된 스킬을 확인한 이용자가 해당 스킬 보유자에게 스킬 요청을 전송
- 스킬명, 희망 진행 시간, 간단한 요청 메시지
- 스킬 보유자는 요청을 수락 또는 거절할 수 있음

### 📌 스킬 교환
- 기능 설명
- 기능 설명
- 기능 설명

### 📌 채팅
- 기능 설명
- 기능 설명
- 기능 설명

### 📌알림
- 기능 설명
- 기능 설명
- 기능 설명

### 📌 크레딧 교환 및 정산
- 스킬 요청이 수락되어 거래가 성사되면 진행 시간 기준으로 크레딧이 자동 정산
- 정산 방식 (예시)
  30분 진행 = 1 크레딧
  스킬 요청 시: 요청자 크레딧 차감
  매칭 성사 시: 스킬 제공자 크레딧 지급 및 거래 확정
- 모든 크레딧 흐름은 서버에서 트랜잭션 기반으로 처리

### 📌리뷰
- 기능 설명
- 기능 설명
- 기능 설명

### 📌 AI 리뷰 요약 (서비스 고도화)
- 스킬 거래 후 남긴 리뷰를 AI가 요약하여 스킬 신뢰도 및 선택 효율 향상

--

## ⚙️ 기술 스택
- OS : Windows11, Ubuntu-22.04-base
- Backend : Java 21, Spring Boot 3.5.9
- Database : MySQL, Redis, Spring Data JPA
- Security : Spring Security, OAuth2.0, JWT
- Test Tool : Postman, JUnit
- DevOps : Docker, docker-compose, Git, GitHub Actions, NCP(Server, ACG), Nginx
- ETC : IntelliJ, Swagger(OpenAPI)

## 📃 API 설계서
- [🔗 Swagger(OpenAPI) LINK ](https://223.130.129.7/swagger-ui/index.html)
- [🔗 Postman API 설계서 LINK](https://documenter.getpostman.com/view/39655317/2sAYX2N4Lg)

## 🛠 ERD
- ![erd.drawio.png](image/erd.png)

## 🛠 아키텍처
- ![architecture.png](image/architecture.png)


## ❓ 기술적 의사결정

<details>
<summary> 1️⃣ Java 21</summary>

</details>

<details>
  <summary> 2️⃣ Spring Boot 3.5.9</summary>

</details>

<details>

  <summary> 3️⃣ MySQL</summary>

</details>

<details>
  <summary> 4️⃣ Redis</summary>

</details>


<details>

  <summary> 5️⃣ GitHub Actions </summary>

</details>


## 📈 성능 개선

## 🚨 트러블 슈팅
### 1️⃣ 트러블 슈팅 1

**[Issues]**
- 이슈 설명
- **[Before] 테스트 결과**:
    - **TPS**: 182.85/sec
    - **평균 요청 응답 시간**: 1.27s

**[Solutions]**
1. **방법 1** : 방법 설명 1
2. **방법 2**: 방법 설명 2
3. **방법 3**: 방법 설명 3

**[Choice] Redis Cache-Aside + Write-Through**
- **이유 1**: 이유 설명 1
- **이유 2**: 이유 설명 2
- **이유 3**: 이유 설명 3

**[Solve]**:
- 해결 방법 1
- 해결 방법 2
- 해결 방법 3

**[After] 성능 테스트 결과**
- **TPS**: 503.35/sec (182.85/sec → 503.35/sec TPS 175% 증가)
- **평균 요청 응답 시간**: 133.1ms (1.27s → 133.1ms 89.5% 감소)
---
### 2️⃣ 트러블 슈팅 2

**[Issues]**
- 이슈 설명
- **[Before] 테스트 결과**:
    - **TPS**: 182.85/sec
    - **평균 요청 응답 시간**: 1.27s

**[Solutions]**
1. **방법 1** : 방법 설명 1
2. **방법 2**: 방법 설명 2
3. **방법 3**: 방법 설명 3

**[Choice] Redis Cache-Aside + Write-Through**
- **이유 1**: 이유 설명 1
- **이유 2**: 이유 설명 2
- **이유 3**: 이유 설명 3

**[Solve]**:
- 해결 방법 1
- 해결 방법 2
- 해결 방법 3

**[After] 성능 테스트 결과**
- **TPS**: 503.35/sec (182.85/sec → 503.35/sec TPS 175% 증가)
- **평균 요청 응답 시간**: 133.1ms (1.27s → 133.1ms 89.5% 감소)
  --
### 3️⃣ 트러블 슈팅 3

**[Issues]**
- 이슈 설명
- **[Before] 테스트 결과**:
    - **TPS**: 182.85/sec
    - **평균 요청 응답 시간**: 1.27s

**[Solutions]**
1. **방법 1** : 방법 설명 1
2. **방법 2**: 방법 설명 2
3. **방법 3**: 방법 설명 3

**[Choice] Redis Cache-Aside + Write-Through**
- **이유 1**: 이유 설명 1
- **이유 2**: 이유 설명 2
- **이유 3**: 이유 설명 3

**[Solve]**:
- 해결 방법 1
- 해결 방법 2
- 해결 방법 3

**[After] 성능 테스트 결과**
- **TPS**: 503.35/sec (182.85/sec → 503.35/sec TPS 175% 증가)
- **평균 요청 응답 시간**: 133.1ms (1.27s → 133.1ms 89.5% 감소)

---

## ✅ 프로젝트 향후 개선 방안

### 1. AI 기반 스킬 추천
- **[현재]**
    - 이용자 요청/거래 이력을 기반으로 AI 추천

- **[이후]**
    - 이후

- **[기대효과]**
    - 스킬 매칭 성공률 증가
    - 사용자 체류 시간 증가
