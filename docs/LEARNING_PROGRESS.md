# 재고관리 학습 진행 문서

이 문서는 **실제 학습 진행 상태**를 코드 재확인 없이 한눈에 파악하기 위한
체크리스트다. 코드 구조/컨벤션/테스트 인프라는 [`ARCHITECTURE.md`](./ARCHITECTURE.md),
각 단계의 확정된 엔티티/메서드/Javadoc 스펙은 [`STAGE_SPECS.md`](./STAGE_SPECS.md)
참고.

이 문서는 매 세션 끝에 갱신한다.

## 현재 학습 체크포인트

| 항목 | 현재 상태 |
|---|---|
| 현재 단계 | 1단계 |
| 현재 사이클 | 체크리스트 공개 전 |
| 마지막 통과 질문 | 없음 |
| 다음 질문 | 1단계 Q1 |
| 다음 행동 | `/study` 실행 시 1단계 배경과 고정 검증 체크리스트를 공개하고 Q1 제시 |

상태 기호는 `⬜`(시작 전), `🔄`(진행 중), `✅`(검증 완료)만 사용한다. 각 단계에서
동시에 `🔄`인 항목은 하나만 허용하며, Q&A 도중에는 위 체크포인트를 질문 단위로
갱신한다.

## 진행 사이클 (매 단계 반복)

1. **배경 설명 + 검증 체크리스트 공개** — 이번 단계 조건을 설명하고,
   `STAGE_SPECS.md`에 미리 고정 작성된 "검증 체크리스트(=테스트 시나리오)"를
   요구사항으로 함께 제시한다. 이 체크리스트는 "무엇을 만족해야 하는가"(불변식/
   요구사항)만 담으며 "어떻게 만족시키는가"(락 종류, 패턴 등)는 담지 않으므로
   힌트가 아니다. 세션마다 동일하게 고정되어 있어 검증 기준이 흔들리지 않는다.
2. **질의응답** — "어떤 문제가 발생할 수 있는가?", "어떻게 해결할 것인가?"를
   개방형 서술 질문으로 묻는다. 사용자가 문제를 인식하고 해결법을 설계한다.
3. **검증** — 1번 체크리스트 항목을 하나씩 대입해 제안된 설계가 통과하는지
   확인한다. 통과하지 못하는 항목이 있으면 2번으로 돌아가 재설계한다
   (2↔3 반복). 체크리스트 자체가 고정돼 있으므로 이 단계의 판정 기준도
   세션마다 동일하다.
4. **스펙 확정** — 체크리스트를 모두 통과한 설계만 `STAGE_SPECS.md`의 해당
   단계 섹션에 엔티티/필드/블랭크 메서드 시그니처/Javadoc 문구로 기록한다.
   **이 문서에 기록되기 전까지는 스펙이 확정된 것이 아니다** — 여러 사람에게
   배포해도 동일한 보일러플레이트가 나오도록, 기록된 스펙이 유일한 생성
   소스가 된다.
5. **보일러플레이트 작성 (나)** — `STAGE_SPECS.md`에 적힌 대로 코드 생성. 핵심
   로직 메서드는 스펙 그대로 `// 구현 진행`만 남기고 비움.
6. **구현 (사용자)** — 비워진 핵심 로직 작성.
7. **테스트 실행 및 검증** — 1번 체크리스트를 그대로 옮긴 통합 테스트로 확인.
   실패 시 5~6 반복.
8. **다음 단계로 이관** — 통과한 최종 코드를 다음 단계 패키지로 복사.

## 커리큘럼 개요

| 단계 | 배경 |
|---|---|
| 1 | 유통 채널 1개·단일 서버 인스턴스, 주문은 항상 결제까지 성공 |
| 2 | 주문↔결제 사이 실패 발생 가능 |
| 3 | 유저 주문 vs 어드민 재고 조작 동시 접근 (다중 인스턴스 배포로 확장) |
| 4 | 3단계와 같은 상황, 상품별 동시 접근 빈도(경합도)가 다양함 |
| 5 | 유통 채널 추가 (단일 서버 유지) |
| 6 | 재고관리 전담 WMS 서버 분리, 통신 실패·타임아웃 가능 |
| 7 | 6단계와 같이 WMS 분리 상태에서, 여러 단계로 구성된 프로세스 중 일부만 실패 가능 |

각 단계에서 실제로 다룰 기법·개념은 해당 단계의 이론 Q&A를 저작하는 시점에
`STAGE_SPECS.md`에 확정해 기록한다. 배포되는 문서에 정답 기법을 미리 나열하지
않기 위해, 저작 전 기획 메모는 이 문서에 남기지 않는다.

동시성 제어는 3단계 전유물이 아니다 — 스프링 애플리케이션은 1단계부터 이미
멀티스레드로 동시 요청을 받으므로, 1단계 Q&A/체크리스트에도 이미 기본
동시성 정합성(락 시점·범위, Lost Update 방지)이 포함되어 있다. 1단계는 서버
인스턴스 1개로 고정해 단일 프로세스 안의 동시 요청만 다루고, 3단계에서
다중 인스턴스 배포로 확장한다. 자세한 내용은 `STAGE_SPECS.md` 1단계·3단계
참고.

### 단계 분리 근거

원래는 5단계였다가 7단계로 나눴다. 판별 기준은 `AGENTS.md` §1(트레이드오프
유무)과 동일하다 — 한 단계 안에 서로 다른 트레이드오프 결정이 여러 개
뭉쳐 있으면, 그건 하나의 단계가 아니라 여러 단계가 압축된 것이다.

- **3단계 → 3 + 4로 분리**: 기존 3단계(유저 주문 vs 어드민 재고 조작 동시
  접근)에는 서로 다른 결이 섞여 있었다. (a) 다중 액터·다중 인스턴스·다중
  리소스로 상황이 "구조적으로 복잡해지는" 문제와, (b) 락 전략 자체를
  바꾸는 문제(비관적 vs 낙관적 선택, 낙관적 락 충돌 시 재시도/백오프)는
  완전히 다른 스킬이다. (a)는 1단계에서 세운 "비관적 락으로 막는다"는
  전제를 더 어려운 구조에 적용하는 것이고, (b)는 그 전제 자체("항상
  비관적 락이 최선인가?")를 재검토하는 것이라 순서상으로도, 난이도상으로도
  분리해야 한 단계가 한 가지 결정에 집중할 수 있다.
- **5단계(WMS 분리) → 6 + 7로 분리**: 기존 5단계에는 (a) 개별 호출 하나를
  안전하게 만드는 "전술적" 스킬(네트워크 실패/타임아웃 대응, 멱등성 있는
  재시도)과, (b) 여러 단계로 구성된 프로세스 전체를 설계하는 "구조적" 스킬
  (최종적 일관성, 사가/보상 트랜잭션)이 섞여 있었다. (a)를 다지지 않고
  (b)로 가면 사가 패턴이 왜 필요한지 체감이 안 된다 — 개별 호출은 이미
  안전한데 왜 프로세스 전체 설계가 또 필요한지를 (a) 이후에 (b)로 넘어가야
  자연스럽게 이해된다.
- 1, 2단계는 이미 하나의 결(각각 "단일 노드 동시성 기초", "비동기 결제와
  상태 전이")로 응집돼 있어 분리하지 않았다.

## 진행 체크리스트

| 단계 | 체크리스트 공개 | 질의응답 | 검증 통과 | 스펙 확정 | 보일러플레이트 | 구현 | 테스트 통과 | 다음 단계 이관 |
|---|---|---|---|---|---|---|---|---|
| 1 | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 2 | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 3 | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 4 | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 5 | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 6 | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| 7 | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |

## 환경 구축 상태 (일회성, 완료됨)

- `build.gradle.kts`: Testcontainers 의존성 추가 완료
  (`org.testcontainers:testcontainers-junit-jupiter`,
  `org.testcontainers:testcontainers-mysql`,
  `org.springframework.boot:spring-boot-testcontainers`). Spring Boot 4.1 BOM이
  관리하는 Testcontainers 2.0.5로 전체 모듈을 정렬했으며 1.x 좌표는 혼용하지
  않는다.
- `stage1` 패키지에 `domain/application/infrastructure/interfaces` 골격
  (`package-info.java`) 생성 완료.
- `OrderInventoryApplication`을 `scanBasePackages`/`@EntityScan`/
  `@EnableJpaRepositories`로 `dh.orderinventory.stage1`에 한정.
- `dh.orderinventory.support.MySqlContainerSupport`(static 초기화 기반 싱글톤
  컨테이너 패턴) + `testconfig.stage1.Stage1TestConfig`(실행 애플리케이션의
  스캔 범위 밖에서 stage1 컴포넌트/엔티티/repository만 스캔하는 전용 부트스트랩
  설정) 작성 완료.
- 스모크 테스트로 `./gradlew test` 통과 확인 완료 (두 테스트가 동일 MySQL
  컨테이너 재사용하는 것도 로그로 확인). 전체 앱 컨텍스트에
  `Stage1TestConfig`가 유입되지 않는지, stage1 전용 컨텍스트가 stage1
  컴포넌트를 실제 스캔하는지도 테스트로 고정함.
- 참고: Spring Boot 4에서 `EntityScan`이
  `org.springframework.boot.autoconfigure.domain` →
  `org.springframework.boot.persistence.autoconfigure`로 패키지 이동됨.

## 현재 생성된 파일 (단계별)

### stage1

- `src/main/java/dh/orderinventory/stage1/package-info.java`
- `src/main/java/dh/orderinventory/stage1/{domain,application,infrastructure,interfaces}/package-info.java`
  (골격만, 아직 실제 클래스 없음)
- `src/test/java/dh/orderinventory/stage1/Stage1EnvironmentSmokeTest.java`
- `src/test/java/dh/orderinventory/testconfig/stage1/Stage1TestConfig.java`

### 공통 환경

- `build.gradle.kts`
- `src/main/java/dh/orderinventory/OrderInventoryApplication.java`
- `src/test/java/dh/orderinventory/OrderInventoryApplicationTests.java`
- `src/test/java/dh/orderinventory/support/MySqlContainerSupport.java`
- `src/test/resources/application.yaml`

## 다음 액션

Claude Code에서 `/study`를 실행한다. AI 튜터가 위 체크포인트를 읽고 1단계 배경 +
검증 체크리스트 공개(`STAGE_SPECS.md`에 이미 고정 작성됨) → 질의응답 →
체크리스트 통과 검증 → 스펙 확정(초안을 그대로 쓸지 수정할지 결정) → 진행
체크리스트 갱신 → 보일러플레이트 생성 순서로 진행한다.
