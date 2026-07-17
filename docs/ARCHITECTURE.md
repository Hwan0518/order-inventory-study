# 재고관리 학습 프로젝트 — 아키텍처 및 환경 설계

이 문서는 **학습 환경(코드 구조/컨벤션/테스트 인프라) 을 어떻게 구현할지**에
대한 기술 설계 문서다. 커리큘럼 내용(각 단계의 배경/문제/진행 상태)은 다루지
않는다 — 그건 [`LEARNING_PROGRESS.md`](./LEARNING_PROGRESS.md) 참고.

## 1. 레이어드 아키텍처

각 단계는 아래 4개 레이어로 구성한다. 의존 방향은
`interfaces → application → domain ← infrastructure`
(도메인은 다른 레이어에 의존하지 않는다).

| 레이어 | 책임 | 예 |
|---|---|---|
| `domain` | 엔티티, 값 객체, 도메인 불변식/규칙, 리포지토리 포트(인터페이스) | `Product`, `Inventory`(증감 불변식을 엔티티 메서드로 가짐), `Order`, `InventoryRepository`(포트) |
| `application` | 유스케이스 오케스트레이션, 트랜잭션 경계(`@Transactional`), 락/재시도 전략 | `OrderService.placeOrder()`, `AdminInventoryService.adjustStock()` |
| `infrastructure` | 기술 종속적 구현 — JPA 리포지토리 구현체, 락 힌트, 외부 시스템 클라이언트 | `InventoryJpaRepository`(포트 구현 + `@Lock` 등), WMS REST 클라이언트(5단계) |
| `interfaces` | 외부 진입점 — REST 컨트롤러, DTO | 1~4단계는 최소/미사용, 5단계에서 order↔WMS 통신용으로 도입 |

핵심 로직(내가 비워두고 사용자가 구현할 부분)은 주로 `domain`(엔티티의 증감
불변식 메서드)과 `application`(유스케이스 흐름, 락/재시도 전략)에 위치한다.
`infrastructure`, `interfaces`는 대부분 보일러플레이트로 취급해 내가 작성한다.

## 2. 패키지/코드 구조 규칙

- 패키지: `dh.orderinventory.stage1` ~ `dh.orderinventory.stage5`, 각각 내부에
  `domain / application / infrastructure / interfaces` 하위 패키지를 둔다.
- 각 단계가 테스트까지 통과하면, 최종 코드를 그대로 다음 단계 패키지로 복사하는
  작업은 내가 수행한다 (중복 타이핑 불필요). 이전 단계 패키지는 삭제하지 않고
  남겨 히스토리 비교가 가능하도록 한다.
- 테이블 이름은 `stage1_inventory`처럼 단계 접두사를 붙여 동일 스키마 내에서
  단계별 엔티티가 섞이지 않게 한다.
- 실행 애플리케이션(`OrderInventoryApplication`)은 항상 **가장 최신 단계**만
  컴포넌트 스캔한다. 이전 단계는 각자의 테스트 설정 클래스(`StageNTestConfig` —
  해당 단계 패키지만 `@ComponentScan`/`@EntityScan`/`@EnableJpaRepositories`)를
  통해 독립적으로 테스트 가능하게 하여 빈 충돌을 막는다. 테스트 설정 자체는
  `dh.orderinventory.testconfig.stageN`에 두어 실행 애플리케이션의 stage 패키지
  스캔에 섞이지 않게 한다.
- 5단계에서는 Gradle 멀티모듈로 전환한다 (`order-service`, `wms-service`, 필요
  시 DTO 공유용 `common` 모듈). `settings.gradle.kts` 변경은 5단계 착수 시점에
  진행한다.

## 3. 구현 스타일 가이드 (보일러플레이트 컨벤션)

**domain 레이어 예시 — 불변식이 핵심 로직인 경우:**

```java
/**
 * [1단계] 재고(Inventory) 도메인 모델
 *
 * <p>배경: 상품 하나당 하나의 재고 수량을 가지며, 주문 시 차감, 입고 시 증가한다.
 * 유통 채널은 1개이고 결제는 항상 성공한다고 가정하므로, 이 시점에서는 재고가
 * 음수가 되는 것만 막으면 된다.</p>
 *
 * <p>구현 목표: 1 이상의 요청 수량만큼 재고를 차감/증가시키되, 차감 시 재고가
 * 0 미만이 되는 경우를 허용하지 않아야 한다. 유효하지 않은 요청은 재고를
 * 변경하지 않아야 한다.</p>
 */
@Entity
@Table(name = "stage1_inventory")
public class Inventory {

    // 필드/생성자 등은 보일러플레이트로 제공

    public void decrease(int quantity) {
        // 구현 진행
    }

    public void increase(int quantity) {
        // 구현 진행
    }
}
```

**application 레이어 예시 — 유스케이스 오케스트레이션이 핵심 로직인 경우:**

```java
/**
 * [1단계] 주문 생성 유스케이스
 *
 * <p>배경: 주문 요청이 들어오면 재고를 확인하고 차감한 뒤 주문을 확정한다.
 * 이 흐름 전체는 하나의 트랜잭션 안에서 원자적으로 처리되어야 한다.</p>
 *
 * <p>구현 목표: 존재하는 상품에 대해 1 이상의 수량과 충분한 재고가 있을 때만
 * 주문과 주문 항목을 생성하고 재고를 차감하라. 유효하지 않은 요청이나 재고 부족
 * 시 어떤 데이터도 변경되지 않아야 한다.</p>
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Order placeOrder(Long productId, int quantity) {
        // 구현 진행
    }
}
```

원칙: Javadoc에는 **배경**과 **구현 목표(무엇을 만족해야 하는가)** 만 적고,
**어떻게(How)** 에 해당하는 힌트(락 종류, 자료구조, 패턴 이름 등)는 절대 적지
않는다.

보일러플레이트를 사람마다 다르게 해석하지 않도록, 확정 스펙에는 Javadoc 외에도
엔티티별 필드 타입/제약/테이블명/생성자, 연관관계 표현 방식, 리포지토리 포트의
정확한 시그니처, 예외 계약, 블랭크 대상과 제공 대상의 경계를 모두 기록한다.
이 항목들은 학습자가 해결할 알고리즘이 아니라 제공 코드의 형태를 고정하는
기계적 계약이다. 어느 하나라도 미정이면 해당 단계는 확정 상태로 전환하지 않는다.

## 4. 공통 기술 스택 및 테스트 전략

- Spring Boot 4.1.0, Java 25, Spring Data JPA, MySQL, Lombok (기존 세팅 유지).
- 통합 테스트: 실제 MySQL 기반. 기존 `compose.yaml`은 로컬 개발 편의용으로
  유지하고, 테스트는 격리성과 반복 가능성을 위해 **Testcontainers(MySQL)** 를
  도입한다 (`org.testcontainers:testcontainers-junit-jupiter`,
  `org.testcontainers:testcontainers-mysql`,
  `org.springframework.boot:spring-boot-testcontainers`). Spring Boot BOM이
  관리하는 동일한 Testcontainers 2.x 버전을 사용하며 1.x 좌표와 혼용하지 않는다.
  환경 구축 시 1회 추가.
- 테스트 JVM 전체에서 MySQL 컨테이너 하나를 공유한다. 공통 추상 테스트 지원
  클래스의 `static final` 필드를 static 초기화 블록에서 수동 `start()`하고,
  `@DynamicPropertySource`는 이미 시작된 컨테이너의 접속 정보만 등록한다. 종료는
  Ryuk에 맡기며 테스트 코드에서 직접 `stop()`하지 않는다.
- 동시성 테스트(3단계~): `ExecutorService` + `CountDownLatch`로 다중 스레드
  요청을 만들고 최종 재고 수량/주문 결과의 정합성을 검증.
- 5단계 통신 실패 시뮬레이션: WMS 클라이언트 호출 실패/타임아웃을 재현하기
  위해 WireMock 또는 실패를 강제하는 테스트용 스텁 서버 사용을 검토.

## 5. 5단계 멀티모듈 전환 계획 (개요만)

- `order-service`: 주문/결제/채널 로직, WMS 클라이언트(interfaces/infrastructure)
- `wms-service`: 재고 데이터/로직 전담, 자체 DB 소유
- `common`(필요 시): 두 서비스가 공유하는 DTO/계약
- 세부 모듈 분리 작업 및 통신 방식(REST 등)은 5단계 착수 시 확정.
