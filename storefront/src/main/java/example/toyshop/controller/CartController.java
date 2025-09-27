package example.toyshop.controller;

import example.toyshop.dto.cart.CartView;
import example.toyshop.model.Cart;
import example.toyshop.service.CartService;
import example.toyshop.service.PaymentServiceClient;
import example.toyshop.service.UserService;
import lombok.RequiredArgsConstructor;
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
public class CartController {

        private final CartService cartService;
        private final PaymentServiceClient paymentServiceClient;
        private final UserService userService;

        /** Получение текущего пользователя из контекста */
        private Mono<UserService.UserWithRoles> getCurrentUser() {
                return ReactiveSecurityContextHolder.getContext()
                                .map(SecurityContext::getAuthentication)
                                .flatMap(auth -> {
                                        if (auth == null || !auth.isAuthenticated()
                                                        || "anonymousUser".equals(auth.getName())) {
                                                return Mono.empty();
                                        }
                                        return userService.findByUsernameWithRoles(auth.getName());
                                })
                                .switchIfEmpty(Mono.error(new RuntimeException("Пользователь не авторизован")));
        }

        /** Отображает корзину пользователя */
        @GetMapping
        public Mono<String> viewCart(Model model) {
                return getCurrentUser()
                                .flatMap(userWithRoles -> {
                                        String userId = userWithRoles.user().getId().toString(); // берём id из user
                                        Mono<CartView> cartMono = cartService.getCartView(userId);
                                        Mono<BalanceResponse> balanceMono = paymentServiceClient.getBalance()
                                                        .onErrorReturn(new BalanceResponse()
                                                                        .balance(BigDecimal.valueOf(0.0)));

                                        return Mono.zip(cartMono, balanceMono)
                                                        .flatMap(tuple -> {
                                                                CartView cartView = tuple.getT1();
                                                                BalanceResponse balance = tuple.getT2();

                                                                model.addAttribute("cart", cartView);
                                                                model.addAttribute("currentBalance",
                                                                                balance.getBalance());

                                                                return Mono.just("cart");
                                                        });
                                });
        }

        /** Добавляет товар в корзину */
        @PostMapping("/add/{productId}")
        public Mono<String> addProduct(@PathVariable Long productId) {
                return getCurrentUser()
                                .flatMap(userWithRoles -> cartService
                                                .addProduct(userWithRoles.user().getId().toString(), productId))
                                .thenReturn("redirect:/products");
        }

        /** Уменьшает количество товара в корзине */
        @PostMapping("/decrease/{productId}")
        public Mono<String> decrease(@PathVariable Long productId) {
                return getCurrentUser()
                                .flatMap(userWithRoles -> cartService
                                                .decreaseProduct(userWithRoles.user().getId().toString(), productId))
                                .thenReturn("redirect:/cart");
        }

        /** Увеличивает количество товара в корзине */
        @PostMapping("/increase/{productId}")
        public Mono<String> increase(@PathVariable Long productId) {
                return getCurrentUser()
                                .flatMap(userWithRoles -> cartService
                                                .addProduct(userWithRoles.user().getId().toString(), productId))
                                .thenReturn("redirect:/cart");
        }

        /** Удаляет товар из корзины */
        @PostMapping("/remove/{productId}")
        public Mono<String> remove(@PathVariable Long productId) {
                return getCurrentUser()
                                .flatMap(userWithRoles -> cartService
                                                .removeProduct(userWithRoles.user().getId().toString(), productId))
                                .thenReturn("redirect:/cart");
        }

        /** Оформляет заказ (checkout) */
        @PostMapping("/checkout")
        public Mono<String> checkout() {
                return getCurrentUser()
                                .flatMap(userWithRoles -> cartService.checkout(userWithRoles.user().getId().toString()))
                                .flatMap(order -> Mono.just("redirect:/orders/" + order.getId()));
        }

        /** Подготавливает данные для оплаты */
        @GetMapping("/prepare-payment")
        @ResponseBody
        public Mono<Object> preparePayment() {
                return getCurrentUser()
                                .flatMap(userWithRoles -> cartService
                                                .getCartView(userWithRoles.user().getId().toString())
                                                .flatMap(cartView -> paymentServiceClient.getBalance()
                                                                .map(balance -> Map.of(
                                                                                "balance", balance.getBalance(),
                                                                                "totalAmount",
                                                                                cartView.getTotalAmount()))));
        }

        /** Подтверждает оплату корзины */
        @PostMapping("/confirm-payment")
        public Mono<String> confirmPayment(ServerWebExchange exchange, Model model) {
                return getCurrentUser()
                                .flatMap(userWithRoles -> cartService
                                                .getCartView(userWithRoles.user().getId().toString())
                                                .flatMap(cartView -> handleCartCheckout(
                                                                userWithRoles.user().getId().toString(), cartView,
                                                                exchange, model)));
        }

        // инициирует процесс оформления корзины и запускает оплату
        private Mono<String> handleCartCheckout(String userId, CartView cartView,
                        ServerWebExchange exchange, Model model) {
                BigDecimal totalAmount = cartView.getTotalAmount();
                return cartService.checkout(userId)
                                .flatMap(cart -> processPayment(cart, totalAmount, cartView, exchange, model));
        }

        // создаёт платежный запрос и отправляет его в платежный сервис
        private Mono<String> processPayment(Cart cart, BigDecimal totalAmount,
                        CartView cartView, ServerWebExchange exchange, Model model) {

                PaymentRequest request = new PaymentRequest();
                request.setOrderId(cart.getId().toString());
                request.setAmount(totalAmount);
                request.setCurrency("RUB");
                request.setMethod("CARD");

                return paymentServiceClient.pay(request)
                                .flatMap(paymentResponse -> handlePaymentResponse(cart, cartView, paymentResponse,
                                                exchange, model));
        }

        // обрабатывает ответ платежного сервиса, обновляет баланс и решает,
        // подтверждать заказ или показывать ошибку
        private Mono<String> handlePaymentResponse(Cart cart, CartView cartView,
                        PaymentResponse paymentResponse,
                        ServerWebExchange exchange, Model model) {

                return paymentServiceClient.getBalance()
                                .flatMap(balance -> {
                                        model.addAttribute("currentBalance", balance.getBalance());

                                        if (PaymentResponse.StatusEnum.SUCCESS.equals(paymentResponse.getStatus())) {
                                                return confirmOrder(cart, cartView, paymentResponse, exchange, model);
                                        } else {
                                                model.addAttribute("errorMessage", paymentResponse.getMessage());
                                                model.addAttribute("cart", cartView);
                                                return Mono.just("cart");
                                        }
                                });
        }

        // подтверждает заказ через платежный сервис и перенаправляет пользователя на
        // страницу заказа или возвращает ошибку
        private Mono<String> confirmOrder(Cart cart, CartView cartView,
                        PaymentResponse paymentResponse,
                        ServerWebExchange exchange, Model model) {

                ConfirmRequest confirmRequest = new ConfirmRequest();
                confirmRequest.setOrderId(cart.getId().toString());
                confirmRequest.setTransactionId(paymentResponse.getTransactionId());

                return paymentServiceClient.confirm(confirmRequest)
                                .flatMap(confirmResponse -> {
                                        if (Boolean.TRUE.equals(confirmResponse.getConfirmed())) {
                                                return Mono.just("redirect:/orders/" + cart.getId());
                                        } else {
                                                model.addAttribute("errorMessage", "Ошибка подтверждения заказа");
                                                model.addAttribute("cart", cartView);
                                                return Mono.just("cart");
                                        }
                                });
        }
}
