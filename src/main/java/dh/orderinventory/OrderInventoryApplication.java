package dh.orderinventory;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


/**
 * 실행 애플리케이션은 항상 가장 최신 단계 패키지만 스캔한다 (docs/ARCHITECTURE.md 2절).
 * 다음 단계로 넘어갈 때는 아래 세 패키지 경로를 최신 stage로 교체한다.
 */
@SpringBootApplication(scanBasePackages = "dh.orderinventory.stage1")
@EntityScan("dh.orderinventory.stage1")
@EnableJpaRepositories("dh.orderinventory.stage1")
public class OrderInventoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderInventoryApplication.class, args);
	}

}
