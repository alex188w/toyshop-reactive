package example.toyshop.controller;

import example.toyshop.IntegrationTestcontainers;
import example.toyshop.model.Cart;
import example.toyshop.model.CartStatus;
import example.toyshop.model.CartItem;
import example.toyshop.model.Product;
import example.toyshop.model.User;
import example.toyshop.repository.CartItemRepository;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import example.toyshop.service.PaymentServiceClient;
import example.toyshop.service.UserService;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.openapi.client.model.BalanceResponse;

@SpringBootTest
@AutoConfigureWebTestClient
// @WebFluxTest(controllers = OrderController.class)
@ActiveProfiles("test")
// @Import(TestSecurityConfig.class)
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

    @MockitoBean
    private UserService userService;

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
        cart = new Cart();
        cart.setUserId("1"); // <-- именно id пользователя в виде строки
        cart.setStatus(CartStatus.COMPLETED);
        cart.setCreatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart).block();

        // Создаём элемент корзины
        CartItem item = new CartItem();
        item.setCartId(cart.getId());
        item.setProductId(product.getId());
        item.setQuantity(2);
        cartItemRepository.save(item).block();

        // Мок для баланса
        Mockito.when(paymentServiceClient.getBalance())
                .thenReturn(Mono.just(new BalanceResponse().balance(BigDecimal.valueOf(1000.0))));

        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");

        UserService.UserWithRoles userWithRoles = new UserService.UserWithRoles(admin, List.of());

        Mockito.when(userService.findByUsernameWithRoles("admin"))
                .thenReturn(Mono.just(userWithRoles));

        // Cart c = cartRepository.findByUserIdAndStatus("admin",
        // CartStatus.COMPLETED).block();
        // System.out.println("Cart found: " + c);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void viewOrders_shouldReturnOrdersPage() {
        // var authority = new SimpleGrantedAuthority("ROLE_ADMIN");
        // var authentication = new UsernamePasswordAuthenticationToken("admin", "1809",
        // List.of(authority));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(res -> org.assertj.core.api.Assertions.assertThat(res.getResponseBody())
                        .contains("Мяч"));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
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
