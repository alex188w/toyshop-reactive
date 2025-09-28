package example.toyshop.controller;

import example.toyshop.config.TestSecurityConfig;
import example.toyshop.dto.cart.CartView;
import example.toyshop.model.User;
import example.toyshop.service.CartPaymentService;
import example.toyshop.service.CartService;
import example.toyshop.service.UserService;
import reactor.core.publisher.Mono;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication;

@WebFluxTest(controllers = CartController.class)
@Import(TestSecurityConfig.class) 
class CartControllerIntegrationTest {

        @Autowired
        private WebTestClient webTestClient;

        @MockitoBean
        private CartService cartService;

        @MockitoBean
        private CartPaymentService cartPaymentService;

        @MockitoBean
        private UserService userService;

        private final String testUserId = "1";

        @BeforeEach
        void setUpUser() {
                // Мокируем пользователя с ролями
                User testUser = new User();
                testUser.setId(1L);
                testUser.setUsername("test");

                UserService.UserWithRoles userWithRoles = new UserService.UserWithRoles(testUser, List.of());

                Mockito.when(userService.findByUsernameWithRoles(Mockito.anyString()))
                                .thenReturn(Mono.just(userWithRoles));

                // Подключаем авторизацию ко всем запросам
                webTestClient = webTestClient.mutateWith(
                                mockAuthentication(new TestingAuthenticationToken("test", "password", "ROLE_USER")));
        }

        @Test
        void viewCart_shouldReturnCartPage() {
                Mockito.when(cartPaymentService.prepareCartForView(Mockito.anyString()))
                                .thenReturn(Mono.just(
                                                Map.of("cart", new CartView(), "currentBalance", BigDecimal.ZERO)));

                webTestClient.get()
                                .uri("/cart")
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(String.class)
                                .consumeWith(res -> assertThat(res.getResponseBody()).contains("cart"));
        }

        @Test
        void addProduct_shouldRedirectToProducts() {
                Mockito.when(cartService.addProduct(Mockito.anyString(), Mockito.anyLong()))
                                .thenReturn(Mono.empty());

                webTestClient.post()
                                .uri("/cart/add/{productId}", 1)
                                .exchange()
                                .expectStatus().is3xxRedirection()
                                .expectHeader().valueEquals("Location", "/products");
        }

        @Test
        void increaseProduct_shouldRedirectToCart() {
                Mockito.when(cartService.increaseProduct(Mockito.anyString(), Mockito.anyLong()))
                                .thenReturn(Mono.empty());

                webTestClient.post()
                                .uri("/cart/increase/{productId}", 1)
                                .exchange()
                                .expectStatus().is3xxRedirection()
                                .expectHeader().valueEquals("Location", "/cart");
        }

        @Test
        void decreaseProduct_shouldRedirectToCart() {
                Mockito.when(cartService.decreaseProduct(Mockito.anyString(), Mockito.anyLong()))
                                .thenReturn(Mono.empty());

                webTestClient.post()
                                .uri("/cart/decrease/{productId}", 1)
                                .exchange()
                                .expectStatus().is3xxRedirection()
                                .expectHeader().valueEquals("Location", "/cart");
        }

        @Test
        void removeProduct_shouldRedirectToCart() {
                Mockito.when(cartService.removeProduct(Mockito.anyString(), Mockito.anyLong()))
                                .thenReturn(Mono.empty());

                webTestClient.post()
                                .uri("/cart/remove/{productId}", 1)
                                .exchange()
                                .expectStatus().is3xxRedirection()
                                .expectHeader().valueEquals("Location", "/cart");
        }

        @Test
        void preparePayment_shouldReturnJson() {
                double mockedBalance = 123.45;
                CartView cartView = new CartView(List.of(), BigDecimal.valueOf(1000));

                Mockito.when(cartPaymentService.prepareCartForView(Mockito.anyString()))
                                .thenReturn(Mono.just(Map.of("cart", cartView, "currentBalance",
                                                BigDecimal.valueOf(mockedBalance))));

                webTestClient.get()
                                .uri("/cart/prepare-payment")
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.currentBalance").isEqualTo(mockedBalance);
        }

        @Test
        void confirmPayment_shouldRedirectToOrder_whenPaymentSucceeds() {
                String redirectUrl = "redirect:/orders/1";
                Mockito.when(cartPaymentService.checkoutAndPay(Mockito.anyString()))
                                .thenReturn(Mono.just(redirectUrl));

                webTestClient.post()
                                .uri("/cart/confirm-payment")
                                .exchange()
                                .expectStatus().is3xxRedirection()
                                .expectHeader().valueEquals("Location", "/orders/1");
        }

        @Test
        void confirmPayment_shouldRedirectIfNoSession() {
                Mockito.when(cartPaymentService.checkoutAndPay(Mockito.anyString()))
                                .thenReturn(Mono.just("redirect:/products"));

                webTestClient.post()
                                .uri("/cart/confirm-payment")
                                .exchange()
                                .expectStatus().is3xxRedirection()
                                .expectHeader().valueEquals("Location", "/products");
        }

        @Test
        void checkout_shouldRedirectAfterPayment() {
                String redirectUrl = "redirect:/orders/1";
                Mockito.when(cartPaymentService.checkoutAndPay(Mockito.anyString()))
                                .thenReturn(Mono.just(redirectUrl));

                webTestClient.post()
                                .uri("/cart/checkout")
                                .exchange()
                                .expectStatus().is3xxRedirection()
                                .expectHeader().valueEquals("Location", "/orders/1");
        }
}
