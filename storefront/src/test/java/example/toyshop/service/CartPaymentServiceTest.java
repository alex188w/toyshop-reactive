package example.toyshop.service;

import example.toyshop.model.Cart;
import example.toyshop.config.TestSecurityConfig;
import example.toyshop.dto.cart.CartView;

import com.example.openapi.client.model.BalanceResponse;
import com.example.openapi.client.model.ConfirmResponse;
import com.example.openapi.client.model.PaymentResponse;
import com.example.openapi.client.model.PaymentResponse.StatusEnum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CartPaymentServiceTest {

        @Mock
        private CartService cartService;

        @Mock
        private PaymentServiceClient paymentServiceClient;

        @InjectMocks
        private CartPaymentService cartPaymentService;

        private Cart cart;
        private CartView cartView;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);

                cart = new Cart();
                cart.setId(1L); // Long вместо UUID
                cartView = new CartView();
                cartView.setTotalAmount(BigDecimal.valueOf(100));
        }

        @Test
        void testPrepareCartForView() {
                when(cartService.getCartView("user1")).thenReturn(Mono.just(cartView));
                when(paymentServiceClient.getBalance())
                                .thenReturn(Mono.just(new BalanceResponse().balance(BigDecimal.valueOf(50))));

                StepVerifier.create(cartPaymentService.prepareCartForView("user1"))
                                .expectNextMatches(map -> map.get("cart") == cartView
                                                && map.get("currentBalance").equals(BigDecimal.valueOf(50)))
                                .verifyComplete();
        }

        @Test
        void testCheckoutAndPay_Success() {
                when(cartService.getCartView("user1")).thenReturn(Mono.just(cartView));
                when(cartService.checkout("user1")).thenReturn(Mono.just(cart));
                when(paymentServiceClient.pay(any())).thenReturn(Mono.just(new PaymentResponse()
                                .transactionId("tx123")
                                .status(StatusEnum.SUCCESS)));
                when(paymentServiceClient.getBalance())
                                .thenReturn(Mono.just(new BalanceResponse().balance(BigDecimal.valueOf(50))));
                when(paymentServiceClient.confirm(any()))
                                .thenReturn(Mono.just(new ConfirmResponse().confirmed(true)));

                StepVerifier.create(cartPaymentService.checkoutAndPay("user1"))
                                .expectNext("redirect:/orders/" + cart.getId())
                                .verifyComplete();
        }

        @Test
        void testCheckoutAndPay_FailedPayment() {
                when(cartService.getCartView("user1")).thenReturn(Mono.just(cartView));
                when(cartService.checkout("user1")).thenReturn(Mono.just(cart));

                // Платёж завершился неуспешно
                when(paymentServiceClient.pay(any())).thenReturn(Mono.just(new PaymentResponse()
                                .transactionId("tx123")
                                .status(StatusEnum.SUCCESS)));

                // Баланс возвращается нормально
                when(paymentServiceClient.getBalance())
                                .thenReturn(Mono.just(new BalanceResponse().balance(BigDecimal.valueOf(50))));

                // confirm() не вызывается, но на всякий случай возвращаем Mono.just
                when(paymentServiceClient.confirm(any()))
                                .thenReturn(Mono.just(new ConfirmResponse().confirmed(false)));

                StepVerifier.create(cartPaymentService.checkoutAndPay("user1"))
                                .expectNext("cart") // возвращается страница с ошибкой
                                .verifyComplete();
        }
}
