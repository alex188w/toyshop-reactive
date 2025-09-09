package example.toyshop.controller;

import example.toyshop.service.CartService;
import example.toyshop.service.PaymentServiceClient;
import example.toyshop.dto.cart.CartView;
import example.toyshop.model.Cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.ui.Model;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;

import com.example.openapi.client.model.BalanceResponse;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CartControllerTest {

    @InjectMocks
    private CartController controller;

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private WebSession webSession;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void viewCart_shouldReturnEmptyCartIfNoSession() {
        when(paymentServiceClient.getBalance())
                .thenReturn(Mono.just(new BalanceResponse().balance(100.0)));

        Mono<String> result = controller.viewCart(null, webSession, model);

        StepVerifier.create(result)
                .expectNext("cart")
                .verifyComplete();

        verify(model).addAttribute(eq("cart"), any(CartView.class));
        verify(model).addAttribute("currentBalance", 100.0);
    }

    @Test
    void viewCart_shouldReturnCartViewIfSessionExists() {
        String sessionId = "session123";
        CartView cartView = new CartView(List.of(), BigDecimal.ZERO);

        when(cartService.getCartView(sessionId)).thenReturn(Mono.just(cartView));
        when(paymentServiceClient.getBalance()).thenReturn(Mono.just(new BalanceResponse().balance(50.0)));

        Mono<String> result = controller.viewCart(sessionId, webSession, model);

        StepVerifier.create(result)
                .expectNext("cart")
                .verifyComplete();

        verify(cartService).getCartView(sessionId);
        verify(model).addAttribute("cart", cartView);
        verify(model).addAttribute("currentBalance", 50.0);
    }

    @Test
    void addProduct_shouldGenerateSessionIdIfMissingAndCallService() {
        String productId = "1";
        when(cartService.addProduct(anyString(), eq(1L))).thenReturn(Mono.empty());
        // Mock добавление cookie
        doNothing().when(response).addCookie(any(ResponseCookie.class));

        Mono<String> result = controller.addProduct(null, 1L, response);

        StepVerifier.create(result)
                .expectNext("redirect:/products")
                .verifyComplete();

        verify(cartService).addProduct(anyString(), eq(1L));
        verify(response).addCookie(any(ResponseCookie.class));
    }

    @Test
    void removeProduct_shouldCallServiceAndRedirect() {
        when(cartService.removeProduct(anyString(), eq(1L))).thenReturn(Mono.empty());
        doNothing().when(response).addCookie(any(ResponseCookie.class));

        Mono<String> result = controller.remove(null, 1L, response);

        StepVerifier.create(result)
                .expectNext("redirect:/cart")
                .verifyComplete();

        verify(cartService).removeProduct(anyString(), eq(1L));
        verify(response).addCookie(any(ResponseCookie.class));
    }

    @Test
    void checkout_shouldRedirectToProductsIfNoSession() {
        Mono<String> result = controller.checkout(null, response);

        StepVerifier.create(result)
                .expectNext("redirect:/products")
                .verifyComplete();
    }

    @Test
    void checkout_shouldRedirectToOrderAndClearCookie() {
        Cart cart = new Cart();
        cart.setId(42L);

        when(cartService.checkout("session123")).thenReturn(Mono.just(cart));
        doNothing().when(response).addCookie(any(ResponseCookie.class));

        Mono<String> result = controller.checkout("session123", response);

        StepVerifier.create(result)
                .expectNext("redirect:/orders/42")
                .verifyComplete();

        verify(cartService).checkout("session123");
        verify(response).addCookie(any(ResponseCookie.class));
    }

    @Test
    void preparePayment_shouldReturnZeroIfNoSession() {
        StepVerifier.create(controller.preparePayment(null))
                .expectNext(Map.of("balance", 0))
                .verifyComplete();
    }

    @Test
    void preparePayment_shouldReturnBalanceAndTotalAmount() {
        CartView cartView = new CartView(List.of(), BigDecimal.valueOf(300));
        when(cartService.getCartView("session123")).thenReturn(Mono.just(cartView));
        when(paymentServiceClient.getBalance()).thenReturn(Mono.just(new BalanceResponse().balance(200.0)));

        StepVerifier.create(controller.preparePayment("session123"))
                .assertNext(map -> {
                    assertEquals(200.0, map.get("balance"));
                    assertEquals(BigDecimal.valueOf(300), map.get("totalAmount"));
                })
                .verifyComplete();
    }

    @Test
    void confirmPayment_shouldRedirectIfNoSession() {
        StepVerifier.create(controller.confirmPayment(null, mock(ServerWebExchange.class), model))
                .expectNext("redirect:/products")
                .verifyComplete();
    }
}
