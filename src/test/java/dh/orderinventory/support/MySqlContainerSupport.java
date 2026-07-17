package dh.orderinventory.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mysql.MySQLContainer;

/**
 * 단계별 통합 테스트가 공유하는 MySQL Testcontainers 지원 (싱글톤 컨테이너 패턴).
 *
 * <p>테스트 클래스가 이 클래스를 상속하면 JVM당 하나의 컨테이너만 기동되고,
 * 데이터소스 프로퍼티가 자동으로 등록된다. 컨테이너는 명시적으로 stop 하지 않으며
 * Testcontainers의 Ryuk 리소스 리퍼가 JVM 종료 시 정리한다.</p>
 */
public abstract class MySqlContainerSupport {

    protected static final MySQLContainer MYSQL_CONTAINER = new MySQLContainer("mysql:8.0")
            .withDatabaseName("order_inventory_test")
            .withUsername("test")
            .withPassword("test");

    static {
        MYSQL_CONTAINER.start();
    }

    @DynamicPropertySource
    static void registerMySqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
    }
}
