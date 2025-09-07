package example.toyshop.controller;

import example.toyshop.service.CartService;
import example.toyshop.dto.cart.CartView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // @Test
    // void viewCart_shouldReturnEmptyCartIfNoSession() {
    //     Mono<String> result = controller.viewCart(null, model);

    //     StepVerifier.create(result)
    //             .expectNext("cart")
    //             .verifyComplete();

    //     verify(model).addAttribute(eq("cart"), any(CartView.class));
    // }

    // @Test
    // void viewCart_shouldReturnCartViewIfSessionExists() {
    //     String sessionId = "session123";
    //     CartView cartView = new CartView(List.of(), BigDecimal.ZERO);

    //     when(cartService.getCartView(sessionId)).thenReturn(Mono.just(cartView));

    //     Mono<String> result = controller.viewCart(sessionId, model);

    //     StepVerifier.create(result)
    //             .expectNext("cart")
    //             .verifyComplete();

    //     verify(cartService).getCartView(sessionId);
    //     verify(model).addAttribute("cart", cartView);
    // }

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
}
