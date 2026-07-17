package dh.orderinventory.stage1;

import dh.orderinventory.support.MySqlContainerSupport;
import dh.orderinventory.testconfig.stage1.Stage1TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * stage1 학습 환경(Stage1TestConfig + Testcontainers MySQL)이 정상적으로
 * 부트스트랩되는지 확인하는 스모크 테스트. 실제 학습 내용(엔티티/유스케이스)이
 * 추가되기 전, 환경 자체의 동작을 검증하기 위한 용도다.
 */
@SpringBootTest(classes = Stage1TestConfig.class)
class Stage1EnvironmentSmokeTest extends MySqlContainerSupport {

    @Autowired
    private Stage1ScanProbe stage1ScanProbe;

    @Test
    void contextLoads() {
        assertThat(stage1ScanProbe).isNotNull();
    }

    @Component
    static class Stage1ScanProbe {
    }
}
