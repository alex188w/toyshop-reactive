package example.toyshop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureWebTestClient
@ExtendWith(SpringExtension.class)
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    public static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static {
        postgres.start();
        // Настройка Spring R2DBC через системные свойства
        System.setProperty("spring.r2dbc.url",
                "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/"
                        + postgres.getDatabaseName());
        System.setProperty("spring.r2dbc.username", postgres.getUsername());
        System.setProperty("spring.r2dbc.password", postgres.getPassword());
    }

    @Autowired
    protected DatabaseClient databaseClient;

    @BeforeEach
    void setupDatabase() {
        // Создаем таблицу product
        databaseClient.sql("""
                    CREATE TABLE IF NOT EXISTS product (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        description TEXT,
                        price INT NOT NULL,
                        image_url VARCHAR(255),
                        quantity INT
                    )
                """).then().block();

        // Создаем таблицу cart
        databaseClient.sql("""
                    CREATE TABLE IF NOT EXISTS cart (
                        id BIGSERIAL PRIMARY KEY,
                        session_id VARCHAR(255),
                        status VARCHAR(20),
                        created_at TIMESTAMP DEFAULT now()
                    )
                """).then().block();

        // Создаем таблицу cart_item
        databaseClient.sql("""
                    CREATE TABLE IF NOT EXISTS cart_item (
                        id BIGSERIAL PRIMARY KEY,
                        cart_id BIGINT REFERENCES cart(id) ON DELETE CASCADE,
                        product_id BIGINT REFERENCES product(id),
                        quantity INT
                    )
                """).then().block();

        // Очистка таблиц перед тестами
        databaseClient.sql("DELETE FROM cart_item").then().block();
        databaseClient.sql("DELETE FROM cart").then().block();
        databaseClient.sql("DELETE FROM product").then().block();
    }
}
