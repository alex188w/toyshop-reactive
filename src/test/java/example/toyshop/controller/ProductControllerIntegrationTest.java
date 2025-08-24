package example.toyshop.controller;

import example.toyshop.AbstractIntegrationTest;
import example.toyshop.model.Product;
import example.toyshop.model.ProductForm;
import example.toyshop.dto.cart.CartView;
import example.toyshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
class ProductControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanDb() {
        productRepository.deleteAll().block();
    }

    // @Test
    // void listProducts_shouldReturnOkAndContainProducts() {
    // // Сохраняем товар
    // productRepository.save(new Product(null, "Мишка", "Плюшевый", 1000, "img",
    // 5)).block();

    // // Делаем GET без корзины
    // webTestClient.get()
    // .uri("/products")
    // .exchange()
    // .expectStatus().isOk()
    // .expectBody(String.class)
    // .consumeWith(res -> assertThat(res.getResponseBody()).contains("Мишка"));
    // }

    @Test
    void listProducts_shouldReturnOkAndContainProducts() {
        // Сохраняем товар в БД
        productRepository.save(new Product(null, "Мишка", "Плюшевый", 1000, "img",
                5)).block();

        // Генерируем sessionId для корзины
        String sessionId = UUID.randomUUID().toString();

        // Делаем GET-запрос с cookie
        webTestClient.get()
                .uri("/products")
                .cookie("CART_SESSION", sessionId) // <-- добавляем cookie
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(res -> assertThat(res.getResponseBody()).contains("Мишка"));
    }

    @Test
    void addProduct_shouldSaveAndRedirect() {
        webTestClient.post()
                .uri("/products/add")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("name=Лего&description=Конструктор&price=2000&quantity=10")
                .exchange()
                .expectStatus().is3xxRedirection();

        Product saved = productRepository.findAll().blockFirst();
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Лего");
    }

    @Test
    void getProduct_shouldReturnProductPage() {
        Product saved = productRepository.save(new Product(null, "Мяч", "Футбольный",
                500, null, 3)).block();

        webTestClient.get()
                .uri("/products/{id}", saved.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(res -> assertThat(res.getResponseBody()).contains("Мяч"));
    }
}
