# 단계별 고정 스펙 (Stage Specs)

이 문서는 각 단계의 **검증 기준과 확정된, 재현 가능한 스펙**을 관리한다.
`상태: 확정`인 섹션에 한해 엔티티/필드, 블랭크 처리할 메서드 시그니처,
Javadoc 문구(배경/구현 목표)까지 여기 적힌 그대로가 실제 보일러플레이트 생성의
유일한 소스다. 초안은 Q&A에서 검증할 후보이며 코드 생성의 근거로 사용하지 않는다.

**중요**: 이 스펙은 라이브 대화에서 즉흥적으로 만들어지지 않는다. 단계별 이론
Q&A는 "이 스펙을 확정하기 위한 저작(authoring) 과정"이며, 한 번 이 문서에
기록되면 이후 보일러플레이트는 항상 이 문서 그대로 생성한다 — 대화 내용이
달라져도 스펙이 바뀌지 않는 한 결과물은 동일하다. 이렇게 하는 이유는 이 학습
자료를 여러 사람에게 배포할 것이기 때문이다 (누가 받든 동일한 보일러플레이트).

각 단계는 두 부분으로 구성된다.

- **검증 체크리스트(= 테스트 시나리오)**: Q&A를 시작하기 전에 먼저 고정
  작성한다. "무엇을 만족해야 하는가"(불변식/요구사항)만 담고 "어떻게
  만족시키는가"(락 종류, 패턴 이름 등 구현 기법)는 담지 않는다 — 그래서
  미리 공개해도 힌트가 아니라 문제 요구사항일 뿐이다. Q&A에서 나온 설계는
  이 체크리스트를 전부 통과해야 스펙으로 확정되며, 나중에 그대로 통합
  테스트 케이스가 된다.
- **확정 스펙**: 체크리스트를 통과한 설계를 엔티티/블랭크 메서드/Javadoc으로
  기록한 것. Q&A 완료 전까지는 "초안" 상태다.

### 확정 전 스펙 완전성 게이트

검증 체크리스트를 통과했더라도 아래 항목을 모두 결정해 기록하기 전에는
`상태: 확정`으로 바꾸거나 보일러플레이트를 생성하지 않는다.

- 각 엔티티의 패키지, 테이블명, 필드명, Java 타입, DB null/unique 제약과 ID
  생성 전략
- 연관관계를 객체 참조로 표현할지 식별자 값으로 표현할지
- JPA 기본 생성자, 제공 생성자/팩토리와 외부에 공개할 getter
- 리포지토리 포트별 정확한 메서드 시그니처와 반환 타입
- infrastructure에서 제공할 JPA adapter 및 Spring Data repository의 경계
- application 유스케이스의 입력/반환 타입, 트랜잭션 경계와 발생 가능한 예외 타입
- 사용자가 구현할 블랭크와 완성된 채로 제공할 보일러플레이트의 정확한 목록
- 블랭크별 Javadoc 최종 문구와 대응하는 검증 체크리스트 항목

[`LEARNING_PROGRESS.md`](./LEARNING_PROGRESS.md) 체크리스트와 이 문서의 단계별
상태(체크리스트 공개 여부/확정 여부)는 항상 같이 갱신한다.

---

## 1단계

배경: 유통 채널 1개, 주문은 항상 결제까지 성공.

### 검증 체크리스트 (= 테스트 시나리오) — 고정

- 재고가 충분할 때 주문이 성공하고, 재고가 정확히 차감된다.
- 재고보다 많은 수량을 주문하면 예외가 발생하고, 주문은 생성되지 않으며,
  재고는 변하지 않는다.
- 재고가 0인 상품을 주문하면 예외가 발생한다.
- 입고(재고 증가) 후 재고 수량에 정확히 반영된다.
- 재고 차감 시, 차감 후 수량이 0 미만이 되는 경우는 허용되지 않는다
  (도메인 단위 테스트로 직접 검증).
- 주문, 재고 차감, 재고 증가에 전달되는 수량은 1 이상이어야 한다. 0 또는 음수면
  예외가 발생하고 재고와 주문 데이터는 변하지 않는다.
- 존재하지 않는 상품을 주문하면 예외가 발생하고 주문 데이터는 생성되지 않는다.
- 한 상품에는 재고 레코드가 하나만 존재해야 한다.
- 성공한 주문의 주문 항목에는 요청한 상품과 수량이 정확히 기록된다.

### AI 튜터 Q&A 스크립트 — 고정/비공개 판정용

AI 튜터는 질문 문구와 순서를 바꾸지 않고 한 번에 하나씩 제시한다. 질문은
사용자에게 공개하지만, 각 질문 아래의 통과 기준과 후속 질문은 판정에만 사용하고
정답처럼 읽어주지 않는다.

#### Q1

질문: `재고를 증가하거나 차감하는 도메인 메서드가 항상 지켜야 할 조건은 무엇인가요?`

통과 기준:

- 증가량과 차감량은 1 이상이어야 한다.
- 차감 후 재고는 0 미만이 될 수 없다.
- 유효하지 않은 요청은 기존 재고를 변경하지 않는다.

부족할 때 후속 질문:

- 수량으로 0이나 음수가 들어오면 허용해도 될까요?
- 현재 재고보다 많이 차감하려는 경우 기존 값은 어떻게 되어야 할까요?

#### Q2

질문: `주문이 성공할 때 상품, 재고, 주문, 주문 항목에는 각각 어떤 확인과 변경이 필요할까요?`

통과 기준:

- 상품 존재 여부와 주문 수량의 유효성을 확인한다.
- 상품에 대응하는 재고를 조회하고 충분한지 확인한다.
- 재고를 정확히 차감한다.
- 주문과 요청 상품·수량이 일치하는 주문 항목을 생성한다.

부족할 때 후속 질문:

- 주문 레코드만 생성하면 어떤 주문인지 충분히 알 수 있을까요?
- 존재하지 않는 상품이나 재고 레코드가 없는 상품은 어떻게 처리해야 할까요?

#### Q3

질문: `재고를 차감한 뒤 주문 저장 중 예외가 발생하면 어떤 결과가 되어야 하며, 그 범위는 어디에서 보장해야 할까요?`

통과 기준:

- 재고, 주문, 주문 항목 변경이 전부 원래 상태로 돌아가야 한다.
- 주문 생성 유스케이스 전체가 하나의 원자적 트랜잭션이어야 한다.
- 트랜잭션 경계는 application의 `OrderService.placeOrder()`에 둔다.

부족할 때 후속 질문:

- 재고만 줄고 주문이 없다면 시스템 상태가 올바른가요?
- 엔티티 메서드 하나가 여러 repository 저장을 함께 원자적으로 만들 수 있을까요?

#### Q4

질문: `재고가 음수가 되지 않는 규칙과 주문 처리 흐름은 각각 어느 레이어에 두어야 하며, 그 이유는 무엇인가요?`

통과 기준:

- 재고 불변식은 `domain`의 `Inventory`에 둔다.
- 주문 흐름과 트랜잭션 오케스트레이션은 `application`의 `OrderService`에 둔다.
- domain은 infrastructure나 interfaces에 의존하지 않는다.

부족할 때 후속 질문:

- 재고 불변식을 서비스에만 두면 다른 진입점에서 재고를 변경할 때도 보장될까요?
- DB 접근과 여러 도메인 객체의 처리 순서를 엔티티 하나가 책임져야 할까요?

Q1~Q4가 모두 통과해야 `질의응답`을 완료로 판정한다. 이후 사용자가 설명한 설계를
검증 체크리스트에 대입하는 별도의 `검증` 게이트를 진행한다.

### 상태: 초안 (Q&A 거쳐 확정 예정)

아래 엔티티/블랭크는 이전 논의(ARCHITECTURE.md 스타일 가이드 예시)를 바탕으로
한 초안이다. 1단계 Q&A에서 위 체크리스트를 통과하는지 확인한 뒤 "확정"으로
전환한다.

### 엔티티 후보 계약 (domain)

모든 엔티티는 `dh.orderinventory.stage1.domain`에 둔다. 연관관계는 JPA 객체
참조가 아니라 식별자 값으로 표현한다. 모든 ID는 MySQL `IDENTITY` 전략의
`Long`이며, JPA용 `protected` 기본 생성자와 아래에 명시한 생성자/getter는
완성된 보일러플레이트로 제공한다. setter는 제공하지 않는다.

| 엔티티 | 테이블 | 필드와 제약 | 제공 생성자 |
|---|---|---|---|
| `Product` | `stage1_product` | `id Long` PK, `name String` not null, length 100 | `Product(String name)` |
| `Inventory` | `stage1_inventory` | `id Long` PK, `productId Long` not null + unique, `quantity int` not null | `Inventory(Long productId, int quantity)` |
| `Order` | `stage1_orders` | `id Long` PK, `createdAt Instant` not null | `Order(Instant createdAt)` |
| `OrderItem` | `stage1_order_item` | `id Long` PK, `orderId Long` not null, `productId Long` not null, `quantity int` not null | `OrderItem(Long orderId, Long productId, int quantity)` |

생성자 인자 자체의 형식 검증(`null`, 빈 상품명, 0 이하 수량)은 완성된
보일러플레이트에서 `IllegalArgumentException`으로 처리한다. 학습자가 구현할
도메인 블랭크는 `Inventory.decrease()`와 `Inventory.increase()`뿐이다.

### 리포지토리 포트 후보 계약 (domain)

아래 포트와 메서드는 블랭크 없이 제공한다. infrastructure에는 각 포트별 JPA
adapter와 Spring Data repository를 제공하며, Spring Data 타입은 domain에
노출하지 않는다.

```java
public interface ProductRepository {
    Optional<Product> findById(Long id);
    Product save(Product product);
}

public interface InventoryRepository {
    Optional<Inventory> findByProductId(Long productId);
    Inventory save(Inventory inventory);
}

public interface OrderRepository {
    Order save(Order order);
}

public interface OrderItemRepository {
    OrderItem save(OrderItem orderItem);
}
```

### application 후보 계약

- `OrderService`는 `ProductRepository`, `InventoryRepository`,
  `OrderRepository`, `OrderItemRepository`를 생성자 주입받는다.
- 공개 유스케이스는 `Order placeOrder(Long productId, int quantity)` 하나다.
- `@Transactional`과 생성자 주입 코드는 제공하며 메서드 본문만 블랭크로 둔다.
- 0 이하 주문 수량은 `IllegalArgumentException`, 상품 또는 재고가 없으면
  `NoSuchElementException`, 재고가 부족하면 `IllegalStateException`을 발생시킨다.
  테스트는 예외 메시지에 의존하지 않는다.
- 입고는 1단계에서 별도 application 유스케이스를 만들지 않고
  `Inventory.increase()` 도메인 단위 테스트와 repository 통합 테스트로 검증한다.

### 블랭크 처리 대상

**`Inventory` (domain) — 불변식**

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
    // 위 후보 계약의 필드/생성자/getter는 보일러플레이트로 제공

    public void decrease(int quantity) {
        // 구현 진행
    }

    public void increase(int quantity) {
        // 구현 진행
    }
}
```

**`OrderService` (application) — 유스케이스**

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

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public Order placeOrder(Long productId, int quantity) {
        // 구현 진행
    }
}
```

---

## 2단계 — 미정

1단계 Q&A/구현/테스트가 끝난 뒤, 2단계 검증 체크리스트 작성 → 이론 Q&A →
이 섹션을 채운다.

## 3단계 — 미정

## 4단계 — 미정

## 5단계 — 미정
