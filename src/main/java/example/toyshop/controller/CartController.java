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

        // Добавить товар
        @PostMapping("/add/{productId}")
        public Mono<String> addProduct(@CookieValue("JSESSIONID") String sessionId,
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
                                .thenReturn("redirect:/products");
        }

        // Уменьшить количество товара
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

        // Увеличить количество товара
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

        // Удалить товар
        @PostMapping("/remove/{productId}")
        public Mono<String> remove(@CookieValue("JSESSIONID") String sessionId,
                        @PathVariable Long productId) {
                return cartService.removeProduct(sessionId, productId)
                                .thenReturn("redirect:/cart");
        }

        @PostMapping("/checkout")
        public Mono<String> checkout(WebSession session, Model model) {
                return cartService.checkout(session.getId())
                                .flatMap(order -> session.invalidate()
                                                .thenReturn(order.getId()))
                                .thenReturn("redirect:/orders"); // + order.getId() - если надо перейти к отдельному
                                                                 // заказу

        }

        // @PostMapping("/checkout")
        // public Mono<String> checkout(ServerWebExchange exchange) {
        // return exchange.getSession()
        // .flatMap(session -> {
        // String sessionId = session.getId();
        // return cartService.checkout(sessionId)
        // .flatMap(orders -> session.invalidate())
        // .thenReturn("redirect:/orders");
        // });
        // }

        // @PostMapping("/checkout")
        public Mono<String> goTo(int id) {
                return Mono.just("redirect:/orders/" + id);
        }

        // @PostMapping("/checkout")
        // public Mono<String> checkout(WebSession session) {
        // String sessionId = session.getId();
        // return cartService.checkout(sessionId)
        // .flatMap(order -> session.invalidate())
        // .then(goTo(198));
        // }

        // @PostMapping("/checkout")
        // public Mono<String> checkout(WebSession session) {
        // String sessionId = session.getId();
        // WebSession order;
        // return cartService.checkout(sessionId)
        // .flatMap(order -> session.invalidate()
        // .then(Mono.just(order.getId())) // возвращаем order после invalidate
        // )
        // .thenReturn("redirect:/orders" + order.getId()); // теперь order.getId()
        // виден
        // }
}
