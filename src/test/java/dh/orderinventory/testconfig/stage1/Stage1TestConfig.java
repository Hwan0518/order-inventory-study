package dh.orderinventory.testconfig.stage1;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 1단계만 독립적으로 부트스트랩하기 위한 테스트 전용 설정 (docs/ARCHITECTURE.md 2절).
 *
 * <p>설정 클래스 자체는 실행 애플리케이션의 스캔 범위 밖에 두고, stage1의 컴포넌트,
 * 엔티티, JPA 리포지토리만 명시적으로 스캔한다. 다른 단계가 추가되어도 이 컨텍스트에
 * 유입되지 않는다.</p>
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan("dh.orderinventory.stage1")
@EntityScan("dh.orderinventory.stage1")
@EnableJpaRepositories("dh.orderinventory.stage1")
public class Stage1TestConfig {
}
