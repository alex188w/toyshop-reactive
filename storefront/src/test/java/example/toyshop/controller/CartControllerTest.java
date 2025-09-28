package example.toyshop.controller;

import example.toyshop.service.CartService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import java.util.List;

import example.toyshop.model.User;
import example.toyshop.service.CartPaymentService;
import example.toyshop.service.UserService;
import org.mockito.Mockito;
import static org.assertj.core.api.Assertions.assertThat;

class CartControllerTest {

    private CartController cartController;

    private CartService cartService;
    private CartPaymentService cartPaymentService;
    private UserService userService;

    private final String testUserId = "1";

    @BeforeEach
    void setUp() {
        cartService = Mockito.mock(CartService.class);
        cartPaymentService = Mockito.mock(CartPaymentService.class);
        userService = Mockito.mock(UserService.class);

        cartController = new CartController(cartService, cartPaymentService, userService);

        // Мокируем UserService для возврата тестового пользователя
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test");

        UserService.UserWithRoles userWithRoles = new UserService.UserWithRoles(testUser, List.of());

        Mockito.when(userService.findByUsernameWithRoles(Mockito.anyString()))
                .thenReturn(Mono.just(userWithRoles));
    }

    @Test
    void addProduct_shouldReturnRedirectToProducts() {
        Mockito.when(cartService.addProduct(testUserId, 1L))
                .thenReturn(Mono.empty());

        String result = cartController.addProduct(1L).block();
        assertThat(result).isEqualTo("redirect:/products");
    }

    @Test
    void increaseProduct_shouldReturnRedirectToCart() {
        Mockito.when(cartService.increaseProduct(testUserId, 1L))
                .thenReturn(Mono.empty());

        String result = cartController.increase(1L).block();
        assertThat(result).isEqualTo("redirect:/cart");
    }

    @Test
    void decreaseProduct_shouldReturnRedirectToCart() {
        Mockito.when(cartService.decreaseProduct(testUserId, 1L))
                .thenReturn(Mono.empty());

        String result = cartController.decrease(1L).block();
        assertThat(result).isEqualTo("redirect:/cart");
    }

    @Test
    void removeProduct_shouldReturnRedirectToCart() {
        Mockito.when(cartService.removeProduct(testUserId, 1L))
                .thenReturn(Mono.empty());

        String result = cartController.remove(1L).block();
        assertThat(result).isEqualTo("redirect:/cart");
    }
}
