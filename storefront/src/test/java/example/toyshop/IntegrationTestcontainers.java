package example.toyshop;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;


public abstract class IntegrationTestcontainers {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("postgres")
            .withPassword("postgres");

    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private DatabaseClient databaseClient;

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

    @BeforeAll
    static void startContainers() {
        postgres.start();
        redis.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Postgres
        String r2dbcUrl = String.format("r2dbc:postgresql://%s:%d/%s",
                postgres.getHost(),
                postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                postgres.getDatabaseName());
        registry.add("spring.r2dbc.url", () -> r2dbcUrl);
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        // Redis
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}
