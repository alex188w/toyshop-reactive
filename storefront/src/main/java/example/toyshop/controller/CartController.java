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

import reactor.core.publisher.Mono;

/**
 * Контроллер для работы с корзиной покупок.
 * <p>
 * Управляет просмотром, добавлением, изменением количества,
 * удалением товаров и оформлением заказа.
 * </p>
 *
 * <p>
 * Идентификатор корзины хранится в cookie {@code CART_SESSION}.  
 * Если cookie отсутствует, при первом действии с корзиной
 * создаётся новый идентификатор и устанавливается cookie.
 * </p>
 *
 * <p><b>Основные эндпоинты:</b></p>
 * <ul>
 *   <li>{@code GET /cart} — просмотр корзины</li>
 *   <li>{@code POST /cart/add/{productId}} — добавить товар</li>
 *   <li>{@code POST /cart/decrease/{productId}} — уменьшить количество товара</li>
 *   <li>{@code POST /cart/increase/{productId}} — увеличить количество товара</li>
 *   <li>{@code POST /cart/remove/{productId}} — удалить товар</li>
 *   <li>{@code POST /cart/checkout} — оформить заказ</li>
 * </ul>
 *
 * @see CartService
 */
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Отображает корзину пользователя.
     *
     * @param sessionId идентификатор корзины из cookie
     * @param model     модель для передачи данных на страницу
     * @return имя HTML-шаблона {@code cart}
     */
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

    /**
     * Добавляет товар в корзину.
     * Если корзина отсутствует, создаётся новая и устанавливается cookie.
     *
     * @param sessionId идентификатор корзины
     * @param productId ID товара
     * @param response  HTTP-ответ для добавления cookie
     * @return редирект на страницу каталога товаров
     */
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

    /**
     * Уменьшает количество товара в корзине.
     *
     * @param sessionId идентификатор корзины
     * @param productId ID товара
     * @param response  HTTP-ответ для добавления cookie
     * @return редирект на страницу корзины
     */
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

    /**
     * Увеличивает количество товара в корзине.
     *
     * @param sessionId идентификатор корзины
     * @param productId ID товара
     * @param response  HTTP-ответ для добавления cookie
     * @return редирект на страницу корзины
     */
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

    /**
     * Удаляет товар из корзины.
     *
     * @param sessionId идентификатор корзины
     * @param productId ID товара
     * @param response  HTTP-ответ для добавления cookie
     * @return редирект на страницу корзины
     */
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

    /**
     * Оформляет заказ и очищает корзину.
     * После успешного checkout cookie {@code CART_SESSION} удаляется.
     *
     * @param sessionId идентификатор корзины
     * @param response  HTTP-ответ для удаления cookie
     * @return редирект на страницу заказа или каталога
     */
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
}
