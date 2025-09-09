package example.toyshop.controller;

import example.toyshop.IntegrationTestcontainers;
import example.toyshop.dto.cart.CartView;
import example.toyshop.model.Cart;
import example.toyshop.model.Product;
import example.toyshop.service.CartService;
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
import com.example.openapi.client.model.ConfirmResponse;
import com.example.openapi.client.model.PaymentResponse;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class CartControllerIntegrationTest extends IntegrationTestcontainers {

        @Autowired
        private WebTestClient webTestClient;

        @MockitoBean
        private CartService cartService;

        @MockitoBean
        private PaymentServiceClient paymentServiceClient;

        private final String sessionId = "test-session";
        private Product testProduct;

        @BeforeEach
        void setUp() {
                testProduct = new Product();
                testProduct.setId(1L);
                testProduct.setName("Игрушечный робот");
                testProduct.setDescription("Робот с батарейками");
                testProduct.setPrice(1500);
                testProduct.setQuantity(10);
        }

        @Test
        void viewCart_shouldReturnEmptyCart() {
                Mockito.when(cartService.getCartView(Mockito.anyString()))
                                .thenReturn(Mono.just(new CartView()));

                Mockito.when(paymentServiceClient.getBalance())
                                .thenReturn(Mono.just(new BalanceResponse().balance(0.0)));

                webTestClient.get()
                                .uri("/cart")
                                .cookie("CART_SESSION", sessionId)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(String.class)
                                .consumeWith(body -> assertThat(body.getResponseBody()).contains("cart"));
        }

        @Test
        void confirmPayment_shouldReturnCartWithError_whenPaymentFails() {
                CartView cartView = new CartView();
                cartView.setTotalAmount(BigDecimal.valueOf(1500));

                Mockito.when(cartService.getCartView(Mockito.anyString()))
                                .thenReturn(Mono.just(cartView));

                Cart cart = new Cart();
                cart.setId(123L);
                Mockito.when(cartService.checkout(Mockito.anyString()))
                                .thenReturn(Mono.just(cart));

                PaymentResponse paymentResponse = new PaymentResponse();
                paymentResponse.setStatus(PaymentResponse.StatusEnum.FAILED);
                paymentResponse.setMessage("Недостаточно средств");
                Mockito.when(paymentServiceClient.pay(Mockito.any()))
                                .thenReturn(Mono.just(paymentResponse));

                Mockito.when(paymentServiceClient.getBalance())
                                .thenReturn(Mono.just(new BalanceResponse().balance(100.0)));

                webTestClient.post()
                                .uri("/cart/confirm-payment")
                                .cookie("CART_SESSION", sessionId)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(String.class)
                                .consumeWith(body -> assertThat(body.getResponseBody()).contains("cart"));
        }

        @Test
        void confirmPayment_shouldRedirectToOrder_whenPaymentSucceeds() {
                Cart cart = new Cart();
                cart.setId(1L);

                CartView cartView = new CartView();
                cartView.setTotalAmount(BigDecimal.valueOf(1500));

                Mockito.when(cartService.getCartView(Mockito.anyString()))
                                .thenReturn(Mono.just(cartView));
                Mockito.when(cartService.checkout(Mockito.anyString()))
                                .thenReturn(Mono.just(cart));

                PaymentResponse paymentResponse = new PaymentResponse();
                paymentResponse.setStatus(PaymentResponse.StatusEnum.SUCCESS);
                paymentResponse.setTransactionId("txn-123");
                Mockito.when(paymentServiceClient.pay(Mockito.any()))
                                .thenReturn(Mono.just(paymentResponse));

                ConfirmResponse confirmResponse = new ConfirmResponse();
                confirmResponse.setConfirmed(true);
                Mockito.when(paymentServiceClient.confirm(Mockito.any()))
                                .thenReturn(Mono.just(confirmResponse));

                Mockito.when(paymentServiceClient.getBalance())
                                .thenReturn(Mono.just(new BalanceResponse().balance(2000.0)));

                webTestClient.post()
                                .uri("/cart/confirm-payment")
                                .cookie("CART_SESSION", sessionId)
                                .exchange()
                                .expectStatus().is3xxRedirection()
                                .expectHeader().valueMatches("Location", "/orders/1");
        }

        @Test
        void viewCart_shouldReturnCartPage() {
                Mockito.when(cartService.getCartView(Mockito.anyString()))
                                .thenReturn(Mono.just(new CartView()));

                Mockito.when(paymentServiceClient.getBalance())
                                .thenReturn(Mono.just(new BalanceResponse().balance(0.0)));

                webTestClient.get()
                                .uri("/cart")
                                .cookie("CART_SESSION", sessionId)
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody(String.class)
                                .consumeWith(res -> assertThat(res.getResponseBody()).contains("cart"));
        }

        @Test
        void addProduct_shouldRedirectToProducts() {
                // Важно: замокать cartService, иначе будет 500
                Mockito.when(cartService.addProduct(Mockito.anyString(), Mockito.anyLong()))
                                .thenReturn(Mono.empty());

                webTestClient.post()
                                .uri("/cart/add/{id}", 1)
                                .cookie("CART_SESSION", sessionId)
                                .exchange()
                                .expectStatus().is3xxRedirection()
                                .expectHeader().value("Location", loc -> assertThat(loc).isEqualTo("/products"));
        }

        @Test
        void preparePayment_shouldReturnJson() {
                double mockedBalance = 123.45;

                Mockito.when(cartService.getCartView(Mockito.anyString()))
                                .thenReturn(Mono.just(new CartView(List.of(), BigDecimal.valueOf(1000))));

                Mockito.when(paymentServiceClient.getBalance())
                                .thenReturn(Mono.just(new BalanceResponse().balance(mockedBalance)));

                webTestClient.get()
                                .uri("/cart/prepare-payment")
                                .cookie("CART_SESSION", "test-session") // <--- вот это ключевое
                                .exchange()
                                .expectStatus().isOk()
                                .expectBody()
                                .jsonPath("$.balance").isEqualTo(mockedBalance);
        }

        @Test
        void confirmPayment_shouldRedirectIfNoSession() {
                webTestClient.post()
                                .uri("/cart/confirm-payment")
                                .exchange()
                                .expectStatus().is3xxRedirection()
                                .expectHeader().valueEquals("Location", "/products");
        }
}
