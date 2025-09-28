package example.toyshop;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import example.toyshop.config.PostgresR2dbcTestcontainer;

@SpringBootTest
@ActiveProfiles("test")
class ToyshopApplicationTests extends PostgresR2dbcTestcontainer {

	@Test
	void contextLoads() {
	}

}
