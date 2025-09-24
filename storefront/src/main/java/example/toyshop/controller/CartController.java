package example.toyshop.controller;

import example.toyshop.dto.cart.CartView;
import example.toyshop.model.Cart;
import example.toyshop.service.CartService;
import example.toyshop.service.PaymentServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;

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

        /** Отображает корзину пользователя */
        @GetMapping
        public Mono<String> viewCart(@AuthenticationPrincipal OidcUser oidcUser,
                        WebSession session,
                        Model model) {
                String userId = oidcUser.getSubject();
                Mono<CartView> cartMono = cartService.getCartView(userId);
                Mono<BalanceResponse> balanceMono = paymentServiceClient.getBalance()
                                .onErrorReturn(new BalanceResponse().balance(0.0));

                return Mono.zip(cartMono, balanceMono)
                                .flatMap(tuple -> {
                                        CartView cartView = tuple.getT1();
                                        BalanceResponse balance = tuple.getT2();

                                        session.getAttributes().put("currentBalance", balance.getBalance());
                                        model.addAttribute("cart", cartView);
                                        model.addAttribute("currentBalance", balance.getBalance());

                                        return Mono.just("cart");
                                });
        }

        /** Добавляет товар в корзину */
        @PostMapping("/add/{productId}")
        public Mono<String> addProduct(@AuthenticationPrincipal OidcUser oidcUser,
                        @PathVariable Long productId) {
                String userId = oidcUser.getSubject();
                return cartService.addProduct(userId, productId)
                                .thenReturn("redirect:/products");
        }

        /** Уменьшает количество товара в корзине */
        @PostMapping("/decrease/{productId}")
        public Mono<String> decrease(@AuthenticationPrincipal OidcUser oidcUser,
                        @PathVariable Long productId) {
                String userId = oidcUser.getSubject();
                return cartService.decreaseProduct(userId, productId)
                                .thenReturn("redirect:/cart");
        }

        /** Увеличивает количество товара в корзине */
        @PostMapping("/increase/{productId}")
        public Mono<String> increase(@AuthenticationPrincipal OidcUser oidcUser,
                        @PathVariable Long productId) {
                String userId = oidcUser.getSubject();
                return cartService.addProduct(userId, productId)
                                .thenReturn("redirect:/cart");
        }

        /** Удаляет товар из корзины */
        @PostMapping("/remove/{productId}")
        public Mono<String> remove(@AuthenticationPrincipal OidcUser oidcUser,
                        @PathVariable Long productId) {
                String userId = oidcUser.getSubject();
                return cartService.removeProduct(userId, productId)
                                .thenReturn("redirect:/cart");
        }

        /** Оформляет заказ (checkout) */
        @PostMapping("/checkout")
        public Mono<String> checkout(@AuthenticationPrincipal OidcUser oidcUser) {
                String userId = oidcUser.getSubject();
                return cartService.checkout(userId)
                                .flatMap(order -> Mono.just("redirect:/orders/" + order.getId()));
        }

        /** Подготавливает данные для оплаты */
        @GetMapping("/prepare-payment")
        @ResponseBody
        public Mono<Object> preparePayment(@AuthenticationPrincipal OidcUser oidcUser) {
                String userId = oidcUser.getSubject();
                return cartService.getCartView(userId)
                                .flatMap(cartView -> paymentServiceClient.getBalance()
                                                .map(balance -> Map.of(
                                                                "balance", balance.getBalance(),
                                                                "totalAmount", cartView.getTotalAmount())));
        }

        /** Подтверждает оплату корзины */
        @PostMapping("/confirm-payment")
        public Mono<String> confirmPayment(@AuthenticationPrincipal OidcUser oidcUser,
                        ServerWebExchange exchange,
                        Model model) {
                String userId = oidcUser.getSubject();
                return cartService.getCartView(userId)
                                .flatMap(cartView -> handleCartCheckout(userId, cartView, exchange, model));
        }

        private Mono<String> handleCartCheckout(String userId, CartView cartView,
                        ServerWebExchange exchange, Model model) {
                double totalAmount = cartView.getTotalAmount().doubleValue();
                return cartService.checkout(userId)
                                .flatMap(cart -> processPayment(cart, totalAmount, cartView, exchange, model));
        }

        private Mono<String> processPayment(
                        Cart cart, double totalAmount,
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

        private Mono<String> handlePaymentResponse(
                        Cart cart, CartView cartView,
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

        private Mono<String> confirmOrder(
                        Cart cart, CartView cartView,
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

// @Controller
// @RequestMapping("/cart")
// @RequiredArgsConstructor
// public class CartController {

// private final CartService cartService;
// private final PaymentServiceClient paymentServiceClient;

// /**
// * Отображает корзину пользователя.
// *
// * @param sessionId идентификатор корзины из cookie
// * @param session веб-сессия для хранения/чтения текущего баланса
// * @param model модель для передачи данных на страницу
// * @return имя HTML-шаблона {@code cart}
// */
// @GetMapping
// public Mono<String> viewCart(@CookieValue(name = "CART_SESSION", required =
// false) String sessionId,
// WebSession session,
// Model model) {
// Mono<CartView> cartMono = (sessionId != null)
// ? cartService.getCartView(sessionId)
// : Mono.just(new CartView());

// // Получаем баланс с paymentService
// Mono<BalanceResponse> balanceMono = paymentServiceClient.getBalance()
// .onErrorReturn(new BalanceResponse().balance(0.0)); // на случай ошибки

// return Mono.zip(cartMono, balanceMono)
// .flatMap(tuple -> {
// CartView cartView = tuple.getT1();
// BalanceResponse balance = tuple.getT2();

// // сохраняем баланс в сессии
// session.getAttributes().put("currentBalance", balance.getBalance());

// model.addAttribute("cart", cartView);
// model.addAttribute("currentBalance", balance.getBalance());

// return Mono.just("cart");
// });
// }

// /**
// * Добавляет товар в корзину.
// * Если корзина отсутствует, создаётся новая и устанавливается cookie.
// *
// * @param sessionId идентификатор корзины
// * @param productId ID товара
// * @param response HTTP-ответ для добавления cookie
// * @return редирект на страницу каталога товаров
// */
// @PostMapping("/add/{productId}")
// public Mono<String> addProduct(@CookieValue(name = "CART_SESSION", required =
// false) String sessionId,
// @PathVariable Long productId,
// ServerHttpResponse response) {

// if (sessionId == null) {
// sessionId = UUID.randomUUID().toString();
// ResponseCookie cookie = ResponseCookie.from("CART_SESSION", sessionId)
// .path("/")
// .httpOnly(true)
// .build();
// response.addCookie(cookie);
// }

// return cartService.addProduct(sessionId, productId)
// .thenReturn("redirect:/products");
// }

// /**
// * Уменьшает количество товара в корзине.
// *
// * @param sessionId идентификатор корзины
// * @param productId ID товара
// * @param response HTTP-ответ для добавления cookie
// * @return редирект на страницу корзины
// */
// @PostMapping("/decrease/{productId}")
// public Mono<String> decrease(@CookieValue(name = "CART_SESSION", required =
// false) String sessionId,
// @PathVariable Long productId,
// ServerHttpResponse response) {
// if (sessionId == null) {
// sessionId = UUID.randomUUID().toString();
// ResponseCookie cookie = ResponseCookie.from("CART_SESSION", sessionId)
// .path("/")
// .httpOnly(true)
// .build();
// response.addCookie(cookie);
// }

// return cartService.decreaseProduct(sessionId, productId)
// .thenReturn("redirect:/cart");
// }

// /**
// * Увеличивает количество товара в корзине.
// *
// * @param sessionId идентификатор корзины
// * @param productId ID товара
// * @param response HTTP-ответ для добавления cookie
// * @return редирект на страницу корзины
// */
// @PostMapping("/increase/{productId}")
// public Mono<String> increase(@CookieValue(name = "CART_SESSION", required =
// false) String sessionId,
// @PathVariable Long productId,
// ServerHttpResponse response) {
// if (sessionId == null) {
// sessionId = UUID.randomUUID().toString();
// ResponseCookie cookie = ResponseCookie.from("CART_SESSION", sessionId)
// .path("/")
// .httpOnly(true)
// .build();
// response.addCookie(cookie);
// }

// return cartService.addProduct(sessionId, productId)
// .thenReturn("redirect:/cart");
// }

// /**
// * Удаляет товар из корзины.
// *
// * @param sessionId идентификатор корзины
// * @param productId ID товара
// * @param response HTTP-ответ для добавления cookie
// * @return редирект на страницу корзины
// */
// @PostMapping("/remove/{productId}")
// public Mono<String> remove(@CookieValue(name = "CART_SESSION", required =
// false) String sessionId,
// @PathVariable Long productId,
// ServerHttpResponse response) {
// if (sessionId == null) {
// sessionId = UUID.randomUUID().toString();
// ResponseCookie cookie = ResponseCookie.from("CART_SESSION", sessionId)
// .path("/")
// .httpOnly(true)
// .build();
// response.addCookie(cookie);
// }

// return cartService.removeProduct(sessionId, productId)
// .thenReturn("redirect:/cart");
// }

// /**
// * Оформляет заказ и очищает корзину.
// * После успешного checkout cookie {@code CART_SESSION} удаляется.
// *
// * @param sessionId идентификатор корзины
// * @param response HTTP-ответ для удаления cookie
// * @return редирект на страницу заказа или каталога
// */
// @PostMapping("/checkout")
// public Mono<String> checkout(@CookieValue(name = "CART_SESSION", required =
// false) String sessionId,
// ServerHttpResponse response) {
// if (sessionId == null) {
// return Mono.just("redirect:/products");
// }

// return cartService.checkout(sessionId)
// .flatMap(order -> {
// // Удаляем cookie после успешного checkout
// ResponseCookie cookie = ResponseCookie.from("CART_SESSION", "")
// .path("/")
// .maxAge(0)
// .build();
// response.addCookie(cookie);

// return Mono.just("redirect:/orders/" + order.getId());
// });
// }

// /**
// * Подготавливает данные для отображения страницы оплаты.
// * <p>
// * Метод получает идентификатор корзины из cookie {@code CART_SESSION}.
// * Если идентификатор отсутствует (корзина не создана), возвращает объект
// * с нулевым балансом.
// * </p>
// * <p>
// * В случае наличия корзины:
// * <ul>
// * <li>Запрашивает текущее состояние корзины у {@link CartService}.</li>
// * <li>Вызывает {@link PaymentServiceClient#getBalance()} для получения
// баланса
// * пользователя.</li>
// * <li>Формирует и возвращает JSON-ответ с ключами:
// * <ul>
// * <li>{@code balance} — доступный баланс пользователя</li>
// * <li>{@code totalAmount} — итоговая сумма товаров в корзине</li>
// * </ul>
// * </li>
// * </ul>
// * </p>
// *
// * @param sessionId идентификатор корзины, извлекаемый из cookie
// * {@code CART_SESSION};
// * может быть {@code null}, если корзина ещё не создана
// * @return реактивный объект {@link Mono}, содержащий карту с данными для
// оплаты
// */
// @GetMapping("/prepare-payment")
// @ResponseBody
// public Mono<Map<String, Object>> preparePayment(
// @CookieValue(name = "CART_SESSION", required = false) String sessionId) {
// if (sessionId == null) {
// return Mono.just(Map.of("balance", 0));
// }

// return cartService.getCartView(sessionId)
// .flatMap(cartView -> paymentServiceClient.getBalance()
// .map(balance -> Map.of(
// "balance", balance.getBalance(),
// "totalAmount", cartView.getTotalAmount())));
// }

// /**
// * Подтверждает оплату корзины.
// * <p>
// * Логика:
// * 1. Если sessionId отсутствует — редирект на каталог.
// * 2. Извлекаем корзину и сумму заказа.
// * 3. Создаём заказ (checkout).
// * 4. Отправляем запрос в платежный сервис.
// * 5. В зависимости от ответа:
// * - при SUCCESS → подтверждаем заказ.
// * - при ошибке оплаты → показываем ошибку на странице корзины.
// * </p>
// *
// * @param sessionId идентификатор корзины из cookie
// * @param exchange объект для работы с HTTP-запросом/ответом (например,
// * управление cookie)
// * @param model модель для передачи данных на страницу
// * @return результат обработки: {@code redirect:/orders/{id}} или {@code cart}
// с
// * ошибкой
// */
// @PostMapping("/confirm-payment")
// public Mono<String> confirmPayment(
// @CookieValue(name = "CART_SESSION", required = false) String sessionId,
// ServerWebExchange exchange,
// Model model) {

// if (sessionId == null) {
// return Mono.just("redirect:/products");
// }

// return cartService.getCartView(sessionId)
// .flatMap(cartView -> handleCartCheckout(sessionId, cartView, exchange,
// model));
// }

// /**
// * Обрабатывает checkout корзины:
// * создаёт заказ и передаёт его дальше на оплату.
// */
// private Mono<String> handleCartCheckout(
// String sessionId, CartView cartView,
// ServerWebExchange exchange, Model model) {

// double totalAmount = cartView.getTotalAmount().doubleValue();

// return cartService.checkout(sessionId)
// .flatMap(cart -> processPayment(cart, totalAmount, cartView, exchange,
// model));
// }

// /**
// * Формирует запрос на оплату и вызывает платежный сервис.
// */
// private Mono<String> processPayment(
// Cart cart, double totalAmount,
// CartView cartView, ServerWebExchange exchange, Model model) {

// PaymentRequest request = new PaymentRequest();
// request.setOrderId(cart.getId().toString());
// request.setAmount(totalAmount);
// request.setCurrency("RUB");
// request.setMethod("CARD");

// return paymentServiceClient.pay(request)
// .flatMap(paymentResponse -> handlePaymentResponse(cart, cartView,
// paymentResponse,
// exchange, model));
// }

// /**
// * Обрабатывает ответ платежного сервиса:
// * если успех — подтверждает заказ,
// * иначе возвращает страницу корзины с ошибкой.
// */
// private Mono<String> handlePaymentResponse(
// Cart cart, CartView cartView,
// PaymentResponse paymentResponse,
// ServerWebExchange exchange, Model model) {

// return paymentServiceClient.getBalance()
// .flatMap(balance -> {
// model.addAttribute("currentBalance", balance.getBalance());

// if (PaymentResponse.StatusEnum.SUCCESS.equals(paymentResponse.getStatus())) {
// return confirmOrder(cart, cartView, paymentResponse, exchange, model);
// } else {
// model.addAttribute("errorMessage", paymentResponse.getMessage());
// model.addAttribute("cart", cartView);
// return Mono.just("cart");
// }
// });
// }

// /**
// * Подтверждает заказ в платежном сервисе.
// * При успехе — удаляет cookie корзины и редиректит на заказ.
// * При неудаче — остаёмся на странице корзины.
// */
// private Mono<String> confirmOrder(
// Cart cart, CartView cartView,
// PaymentResponse paymentResponse,
// ServerWebExchange exchange, Model model) {

// ConfirmRequest confirmRequest = new ConfirmRequest();
// confirmRequest.setOrderId(cart.getId().toString());
// confirmRequest.setTransactionId(paymentResponse.getTransactionId());

// return paymentServiceClient.confirm(confirmRequest)
// .flatMap(confirmResponse -> {
// if (Boolean.TRUE.equals(confirmResponse.getConfirmed())) {
// exchange.getResponse()
// .addCookie(ResponseCookie.from("CART_SESSION", "")
// .path("/")
// .maxAge(0)
// .build());
// return Mono.just("redirect:/orders/" + cart.getId());
// } else {
// model.addAttribute("errorMessage", "Ошибка подтверждения заказа");
// model.addAttribute("cart", cartView);
// return Mono.just("cart");
// }
// });
// }
// }
