package example.toyshop.controller;

import example.toyshop.dto.cart.CartView;
import example.toyshop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpResponse;
import java.util.UUID;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;

import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

        private final CartService cartService;

        // Просмотр корзины
        @GetMapping
        public Mono<String> viewCart(@CookieValue(name = "CART_SESSION", required = false) String sessionId,
                        Model model) {
                if (sessionId == null) {
                        model.addAttribute("cart", new CartView());
                        return Mono.just("cart");
                }

                return cartService.getCartView(sessionId)
                                .map(cartView -> {
                                        model.addAttribute("cart", cartView);
                                        return "cart";
                                });
        }

        // Добавить товар
        @PostMapping("/add/{productId}")
        public Mono<String> addProduct(@CookieValue(name = "CART_SESSION", required = false) String sessionId,
                        @PathVariable Long productId,
                        ServerHttpResponse response) {

                if (sessionId == null) {
                        sessionId = UUID.randomUUID().toString();
                        ResponseCookie cookie = ResponseCookie.from("CART_SESSION", sessionId)
                                        .path("/")
                                        .httpOnly(true)
                                        .build();
                        response.addCookie(cookie);
                }

                return cartService.addProduct(sessionId, productId)
                                .thenReturn("redirect:/products");
        }

        // Уменьшить количество товара
        @PostMapping("/decrease/{productId}")
        public Mono<String> decrease(@CookieValue(name = "CART_SESSION", required = false) String sessionId,
                        @PathVariable Long productId,
                        ServerHttpResponse response) {
                if (sessionId == null) {
                        sessionId = UUID.randomUUID().toString();
                        ResponseCookie cookie = ResponseCookie.from("CART_SESSION", sessionId)
                                        .path("/")
                                        .httpOnly(true)
                                        .build();
                        response.addCookie(cookie);
                }

                return cartService.decreaseProduct(sessionId, productId)
                                .thenReturn("redirect:/cart");
        }

        // Увеличить количество товара
        @PostMapping("/increase/{productId}")
        public Mono<String> increase(@CookieValue(name = "CART_SESSION", required = false) String sessionId,
                        @PathVariable Long productId,
                        ServerHttpResponse response) {
                if (sessionId == null) {
                        sessionId = UUID.randomUUID().toString();
                        ResponseCookie cookie = ResponseCookie.from("CART_SESSION", sessionId)
                                        .path("/")
                                        .httpOnly(true)
                                        .build();
                        response.addCookie(cookie);
                }

                return cartService.addProduct(sessionId, productId)
                                .thenReturn("redirect:/cart");
        }

        // Удалить товар
        @PostMapping("/remove/{productId}")
        public Mono<String> remove(@CookieValue(name = "CART_SESSION", required = false) String sessionId,
                        @PathVariable Long productId,
                        ServerHttpResponse response) {
                if (sessionId == null) {
                        sessionId = UUID.randomUUID().toString();
                        ResponseCookie cookie = ResponseCookie.from("CART_SESSION", sessionId)
                                        .path("/")
                                        .httpOnly(true)
                                        .build();
                        response.addCookie(cookie);
                }

                return cartService.removeProduct(sessionId, productId)
                                .thenReturn("redirect:/cart");
        }

        @PostMapping("/checkout")
        public Mono<String> checkout(@CookieValue(name = "CART_SESSION", required = false) String sessionId,
                        ServerHttpResponse response) {
                if (sessionId == null) {
                        return Mono.just("redirect:/products");
                }

                return cartService.checkout(sessionId)
                                .flatMap(order -> {
                                        // Удаляем cookie после успешного checkout
                                        ResponseCookie cookie = ResponseCookie.from("CART_SESSION", "")
                                                        .path("/")
                                                        .maxAge(0)
                                                        .build();
                                        response.addCookie(cookie);

                                        return Mono.just("redirect:/orders/" + order.getId());
                                });
        }

        // public Mono<String> goTo(int id) {
        //         return Mono.just("redirect:/orders/" + id);
        // }
}
