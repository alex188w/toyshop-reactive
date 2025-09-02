package example.toyshop.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@TestConfiguration
public class PostgresR2dbcTestcontainer {

    /**
     * Создаем отдельный контейнер PostgreSQL для каждого класса тестов.
     * Spring Boot подхватит его автоматически через @ServiceConnection для R2DBC.
     */
    // @Container
    // @ServiceConnection
    // @SuppressWarnings("resource")
    // static final PostgreSQLContainer<?> postgresContainer() {
    // PostgreSQLContainer<?> container = new
    // PostgreSQLContainer<>("postgres:15-alpine")
    // .withDatabaseName("testdb")
    // .withUsername("test")
    // .withPassword("test");
    // container.start();
    // return container;
    // }

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
}
