package example.toyshop.service;

import com.example.openapi.client.model.BalanceResponse;
import com.example.openapi.client.model.ConfirmResponse;
import com.example.openapi.client.model.PaymentResponse;
import com.example.openapi.client.model.PaymentResponse.StatusEnum;

import example.toyshop.model.Cart;
import example.toyshop.config.TestSecurityConfig;
import example.toyshop.dto.cart.CartView;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(controllers = CartPaymentService.class)
@Import(TestSecurityConfig.class)
class CartPaymentServiceIntegrationTest {

    @Autowired
    private CartPaymentService cartPaymentService;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private PaymentServiceClient paymentServiceClient;

    private final String userId = "1";

    private Cart cart;
    private CartView cartView;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(1L);

        cartView = new CartView();
        cartView.setItems(List.of());
        cartView.setTotalAmount(BigDecimal.valueOf(1500));
    }

    @Test
    void prepareCartForView_shouldReturnCartAndBalance() {
        Mockito.when(cartService.getCartView(userId)).thenReturn(Mono.just(cartView));
        Mockito.when(paymentServiceClient.getBalance())
                .thenReturn(Mono.just(new BalanceResponse().balance(BigDecimal.valueOf(500))));

        Mono<Map<String, Object>> result = cartPaymentService.prepareCartForView(userId);

        StepVerifier.create(result)
                .assertNext(map -> {
                    assertThat(map.get("cart")).isEqualTo(cartView);
                    assertThat(map.get("currentBalance")).isEqualTo(BigDecimal.valueOf(500));
                })
                .verifyComplete();
    }

    @Test
    void checkoutAndPay_shouldRedirectToOrder_whenPaymentSucceeds() {
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setStatus(PaymentResponse.StatusEnum.SUCCESS);
        paymentResponse.setTransactionId("txn-123");

        ConfirmResponse confirmResponse = new ConfirmResponse();
        confirmResponse.setConfirmed(true);

        Mockito.when(cartService.getCartView(userId)).thenReturn(Mono.just(cartView));
        Mockito.when(cartService.checkout(userId)).thenReturn(Mono.just(cart));
        Mockito.when(paymentServiceClient.pay(Mockito.any())).thenReturn(Mono.just(paymentResponse));
        Mockito.when(paymentServiceClient.confirm(Mockito.any())).thenReturn(Mono.just(confirmResponse));
        Mockito.when(paymentServiceClient.getBalance())
                .thenReturn(Mono.just(new BalanceResponse().balance(BigDecimal.valueOf(500))));

        Mono<String> result = cartPaymentService.checkoutAndPay(userId);

        StepVerifier.create(result)
                .expectNext("redirect:/orders/1")
                .verifyComplete();
    }

    @Test
    void checkoutAndPay_shouldReturnCart_whenPaymentFails() {
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setStatus(PaymentResponse.StatusEnum.FAILED);

        Mockito.when(cartService.getCartView(userId)).thenReturn(Mono.just(cartView));
        Mockito.when(cartService.checkout(userId)).thenReturn(Mono.just(cart));

        // pay возвращает FAILURE
        Mockito.when(paymentServiceClient.pay(Mockito.any())).thenReturn(Mono.just(paymentResponse));

        // balance должен быть Mono, иначе flatMap упадет
        Mockito.when(paymentServiceClient.getBalance())
                .thenReturn(Mono.just(new BalanceResponse().balance(BigDecimal.valueOf(500))));

        StepVerifier.create(cartPaymentService.checkoutAndPay(userId))
                .expectNext("cart") // как раз возвращается "cart" при FAILURE
                .verifyComplete();
    }
}
