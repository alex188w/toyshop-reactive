package example.toyshop.controller;

import example.toyshop.dto.cart.CartView;
import example.toyshop.model.Cart;
import example.toyshop.service.CartPaymentService;
import example.toyshop.service.CartService;
import example.toyshop.service.PaymentServiceClient;
import example.toyshop.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import com.example.openapi.client.model.BalanceResponse;
import com.example.openapi.client.model.ConfirmRequest;
import com.example.openapi.client.model.PaymentRequest;
import com.example.openapi.client.model.PaymentResponse;

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
 * <p>
 * <b>Основные эндпоинты:</b>
 * </p>
 * <ul>
 * <li>{@code GET /cart} — просмотр корзины</li>
 * <li>{@code POST /cart/add/{productId}} — добавить товар</li>
 * <li>{@code POST /cart/decrease/{productId}} — уменьшить количество
 * товара</li>
 * <li>{@code POST /cart/increase/{productId}} — увеличить количество
 * товара</li>
 * <li>{@code POST /cart/remove/{productId}} — удалить товар</li>
 * <li>{@code POST /cart/checkout} — оформить заказ</li>
 * </ul>
 *
 * @see CartService
 */

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CartController {

        private final CartService cartService;
        private final CartPaymentService cartPaymentService;
        private final UserService userService;

        /** Получение текущего пользователя из контекста */
        private Mono<String> getUserId() {
                return ReactiveSecurityContextHolder.getContext()
                                .map(SecurityContext::getAuthentication)
                                .flatMap(auth -> {
                                        if (auth == null || !auth.isAuthenticated()
                                                        || "anonymousUser".equals(auth.getName())) {
                                                return Mono.empty();
                                        }
                                        return userService.findByUsernameWithRoles(auth.getName())
                                                        .map(u -> u.user().getId().toString());
                                });
        }

        /** Отображает корзину пользователя */
        @GetMapping
        public Mono<String> viewCart(Model model) {
                return getUserId()
                                .flatMap(userId -> cartPaymentService.prepareCartForView(userId)
                                                .doOnNext(map -> {
                                                        model.addAttribute("cart", map.get("cart"));
                                                        model.addAttribute("currentBalance", map.get("currentBalance"));
                                                })
                                                .thenReturn("cart"));
        }

        /** Добавляет товар в корзину */
        @PostMapping("/add/{productId}")
        public Mono<String> addProduct(@PathVariable Long productId) {
                return getUserId()
                                .flatMap(userId -> cartService.addProduct(userId, productId))
                                .thenReturn("redirect:/products");
        }

        /** Уменьшает количество товара в корзине */
        @PostMapping("/decrease/{productId}")
        public Mono<String> decrease(@PathVariable Long productId) {
                return getUserId()
                                .flatMap(userId -> cartService
                                                .decreaseProduct(userId, productId))
                                .thenReturn("redirect:/cart");
        }

        /** Увеличивает количество товара в корзине */
        @PostMapping("/increase/{productId}")
        public Mono<String> increase(@PathVariable Long productId) {
                return getUserId()
                                .flatMap(userId -> cartService.increaseProduct(userId, productId))
                                .then(Mono.just("redirect:/cart"));
        }

        /** Удаляет товар из корзины */
        @PostMapping("/remove/{productId}")
        public Mono<String> remove(@PathVariable Long productId) {
                return getUserId()
                                .flatMap(userId -> cartService
                                                .removeProduct(userId, productId))
                                .thenReturn("redirect:/cart");
        }

        /** Оформляет заказ (checkout) */
        @PostMapping("/checkout")
        public Mono<String> checkout() {
                return getUserId()
                                .flatMap(cartPaymentService::checkoutAndPay);
        }

        /** Подготавливает данные для оплаты */
        @GetMapping("/prepare-payment")
        @ResponseBody
        public Mono<Object> preparePayment() {
                return getUserId()
                                .flatMap((userId -> cartPaymentService.prepareCartForView(userId)));
        }

        /** Подтверждает оплату корзины */
        @PostMapping("/confirm-payment")
        public Mono<String> confirmPayment() {
                return getUserId()
                                .flatMap(userId -> cartPaymentService.checkoutAndPay(userId));
        }
}
