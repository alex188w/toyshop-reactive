package example.toyshop.controller;

import example.toyshop.AbstractIntegrationTest;
import example.toyshop.config.PostgresR2dbcTestcontainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(PostgresR2dbcTestcontainer.class)
public class CartControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void viewCart_shouldReturnEmptyCartIfNoSession() {
        webTestClient.get()
                .uri("/cart")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(res -> assertThat(res.getResponseBody())
                        .contains("cart")); // проверяем, что рендерится cart.html
    }

    @Test
    void addProduct_shouldAddProductToCart() {
        // Создаем товар
        Long productId = databaseClient.sql("""
                INSERT INTO product(name, description, price, image_url, quantity)
                VALUES ('Мяч', 'Футбольный', 500, null, 5) RETURNING id
        """).map((row, meta) -> row.get("id", Long.class))
          .one()
          .block();

        String sessionId = UUID.randomUUID().toString();

        // Добавляем товар
        webTestClient.post()
                .uri("/cart/add/{productId}", productId)
                .cookie("CART_SESSION", sessionId)
                .exchange()
                .expectStatus().is3xxRedirection();

        // Проверяем, что товар появился в cart_item
        Long count = databaseClient.sql("SELECT COUNT(*) AS c FROM cart_item")
                .map((row, meta) -> row.get("c", Long.class))
                .one()
                .block();

        assertThat(count).isEqualTo(1);
    }

    @Test
    void removeProduct_shouldRemoveProductFromCart() {
        // Создаем товар и корзину
        Long productId = databaseClient.sql("""
                INSERT INTO product(name, description, price, image_url, quantity)
                VALUES ('Кубик', 'Игрушка', 100, null, 5) RETURNING id
        """).map((row, meta) -> row.get("id", Long.class))
          .one()
          .block();

        Long cartId = databaseClient.sql("""
                INSERT INTO cart(session_id, status)
                VALUES ('session123', 'ACTIVE') RETURNING id
        """).map((row, meta) -> row.get("id", Long.class))
          .one()
          .block();

        databaseClient.sql("""
                INSERT INTO cart_item(cart_id, product_id, quantity)
                VALUES (:cartId, :productId, 1)
        """).bind("cartId", cartId)
          .bind("productId", productId)
          .then().block();

        // Удаляем товар
        webTestClient.post()
                .uri("/cart/remove/{productId}", productId)
                .cookie("CART_SESSION", "session123")
                .exchange()
                .expectStatus().is3xxRedirection();

        // Проверяем, что cart_item пуст
        Long count = databaseClient.sql("SELECT COUNT(*) AS c FROM cart_item")
                .map((row, meta) -> row.get("c", Long.class))
                .one()
                .block();

        assertThat(count).isEqualTo(0);
    }

    @Test
    void checkout_shouldRedirectToOrderAndClearSessionCookie() {
        // Создаем корзину
        Long cartId = databaseClient.sql("""
                INSERT INTO cart(session_id, status)
                VALUES ('sessionXYZ', 'ACTIVE') RETURNING id
        """).map((row, meta) -> row.get("id", Long.class))
          .one()
          .block();

        webTestClient.post()
                .uri("/cart/checkout")
                .cookie("CART_SESSION", "sessionXYZ")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Set-Cookie", "CART_SESSION=;.*"); // проверяем очистку cookie
    }
}
