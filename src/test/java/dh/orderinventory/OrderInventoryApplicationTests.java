package dh.orderinventory;


import dh.orderinventory.support.MySqlContainerSupport;
import dh.orderinventory.testconfig.stage1.Stage1TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
class OrderInventoryApplicationTests extends MySqlContainerSupport {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void contextLoads() {
		assertThat(applicationContext.getBeansOfType(Stage1TestConfig.class)).isEmpty();
	}

}
