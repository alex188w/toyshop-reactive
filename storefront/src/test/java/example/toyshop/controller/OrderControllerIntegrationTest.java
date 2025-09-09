package example.toyshop.controller;

import example.toyshop.IntegrationTestcontainers;
import example.toyshop.model.Cart;
import example.toyshop.model.CartStatus;
import example.toyshop.model.CartItem;
import example.toyshop.model.Product;
import example.toyshop.repository.CartItemRepository;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import example.toyshop.service.PaymentServiceClient;
import reactor.core.publisher.Mono;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.openapi.client.model.BalanceResponse;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class OrderControllerIntegrationTest extends IntegrationTestcontainers {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private PaymentServiceClient paymentServiceClient;

    private Cart cart;
    private Product product;

    @BeforeEach
    void setupData() {
        // Очистка
        cartItemRepository.deleteAll().block();
        cartRepository.deleteAll().block();
        productRepository.deleteAll().block();

        // Создаём продукт
        product = new Product();
        product.setName("Мяч");
        product.setPrice(500);
        product.setQuantity(10);
        product = productRepository.save(product).block();

        // Создаём корзину со статусом COMPLETED
        cart = new Cart();
        cart.setSessionId("test-session");
        cart.setStatus(CartStatus.COMPLETED);
        cart = cartRepository.save(cart).block();

        // Создаём элемент корзины
        CartItem item = new CartItem();
        item.setCartId(cart.getId());
        item.setProductId(product.getId());
        item.setQuantity(2);
        cartItemRepository.save(item).block();

        // Мок для баланса
        Mockito.when(paymentServiceClient.getBalance())
                .thenReturn(Mono.just(new BalanceResponse().balance(1000.0)));
    }

    @Test
    void viewOrders_shouldReturnOrdersPage() {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(res -> org.assertj.core.api.Assertions.assertThat(res.getResponseBody())
                        .contains("Мяч"));
    }

    @Test
    void viewOrder_shouldReturnSingleOrder() {
        webTestClient.get()
                .uri("/orders/{id}", cart.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(res -> org.assertj.core.api.Assertions.assertThat(res.getResponseBody())
                        .contains("Мяч"));
    }
}
