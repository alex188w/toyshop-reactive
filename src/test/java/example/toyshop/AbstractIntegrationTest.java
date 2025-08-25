package example.toyshop;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;


@SpringBootTest
@AutoConfigureWebTestClient
// @ExtendWith(SpringExtension.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected DatabaseClient databaseClient;

    @BeforeEach
    void setupDatabase() {
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

        databaseClient.sql("""
            CREATE TABLE IF NOT EXISTS cart (
                id BIGSERIAL PRIMARY KEY,
                session_id VARCHAR(255),
                status VARCHAR(20),
                created_at TIMESTAMP DEFAULT now()
            )
        """).then().block();

        databaseClient.sql("""
            CREATE TABLE IF NOT EXISTS cart_item (
                id BIGSERIAL PRIMARY KEY,
                cart_id BIGINT REFERENCES cart(id) ON DELETE CASCADE,
                product_id BIGINT REFERENCES product(id),
                quantity INT
            )
        """).then().block();

        databaseClient.sql("DELETE FROM cart_item").then().block();
        databaseClient.sql("DELETE FROM cart").then().block();
        databaseClient.sql("DELETE FROM product").then().block();
    }
}