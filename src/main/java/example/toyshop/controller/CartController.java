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
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

        private final CartService cartService;

        // просмотр корзины
        @GetMapping
        public Mono<String> viewCart(@CookieValue("JSESSIONID") String sessionId, Model model) {
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

        // добавить товар
        @PostMapping("/add/{productId}")
        public Mono<String> addProduct(@CookieValue("JSESSIONID") String sessionId,
                        @PathVariable Long productId,
                        ServerHttpResponse response) {
                if (sessionId == null) {
                        sessionId = UUID.randomUUID().toString(); // генерируем новый ID
                        ResponseCookie cookie = ResponseCookie.from("SESSION", sessionId)
                                        .path("/")
                                        .httpOnly(true)
                                        .build();
                        response.addCookie(cookie);
                }

                return cartService.addProduct(sessionId, productId)
                                .thenReturn("redirect:/products");
        }

        // уменьшить количество
        @PostMapping("/decrease/{productId}")
        public Mono<String> decrease(@CookieValue("JSESSIONID") String sessionId,
                        @PathVariable Long productId,
                        ServerHttpResponse response) {
                if (sessionId == null) {
                        sessionId = UUID.randomUUID().toString();
                        ResponseCookie cookie = ResponseCookie.from("SESSION", sessionId)
                                        .path("/")
                                        .httpOnly(true)
                                        .build();
                        response.addCookie(cookie);
                }
                return cartService.decreaseProduct(sessionId, productId)
                                .thenReturn("redirect:/cart");
        }

        // увеличить количество
        @PostMapping("/increase/{productId}")
        public Mono<String> increase(@CookieValue("JSESSIONID") String sessionId,
                        @PathVariable Long productId,
                        ServerHttpResponse response) {
                if (sessionId == null) {
                        sessionId = UUID.randomUUID().toString();
                        ResponseCookie cookie = ResponseCookie.from("SESSION", sessionId)
                                        .path("/")
                                        .httpOnly(true)
                                        .build();
                        response.addCookie(cookie);
                }
                return cartService.addProduct(sessionId, productId)
                                .thenReturn("redirect:/cart");
        }

        // удалить товар
        @PostMapping("/remove/{productId}")
        public Mono<String> remove(@CookieValue("JSESSIONID") String sessionId,
                        @PathVariable Long productId) {
                return cartService.removeProduct(sessionId, productId)
                                .thenReturn("redirect:/cart");
        }

        // оформить заказ — пока просто очищаем корзину
        @PostMapping("/checkout")
        public Mono<String> checkout(@CookieValue("JSESSIONID") String sessionId) {
                // В сервисе метода checkout пока нет, можно просто удалить все товары
                return cartService.getActiveCart(sessionId)
                                .flatMap(cart -> cartService.getCartView(sessionId)
                                                .flatMap(cartView -> Mono.when(
                                                                cartView.getItems().stream()
                                                                                .map(item -> cartService.removeProduct(
                                                                                                sessionId,
                                                                                                item.getProductId()))
                                                                                .toList())))
                                .thenReturn("redirect:/orders");
        }
}
