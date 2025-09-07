package example.toyshop.controller;

import example.toyshop.dto.cart.CartView;
import example.toyshop.service.CartService;
import example.toyshop.service.PaymentServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpResponse;

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

    /**
     * Отображает корзину пользователя.
     *
     * @param sessionId идентификатор корзины из cookie
     * @param session   веб-сессия для хранения/чтения текущего баланса
     * @param model     модель для передачи данных на страницу
     * @return имя HTML-шаблона {@code cart}
     */
    @GetMapping
    public Mono<String> viewCart(@CookieValue(name = "CART_SESSION", required = false) String sessionId,
            WebSession session,
            Model model) {
        Mono<CartView> cartMono = (sessionId != null)
                ? cartService.getCartView(sessionId)
                : Mono.just(new CartView());

        // Получаем баланс с paymentService
        Mono<BalanceResponse> balanceMono = paymentServiceClient.getBalance()
                .onErrorReturn(new BalanceResponse().balance(0.0)); // на случай ошибки

        return Mono.zip(cartMono, balanceMono)
                .flatMap(tuple -> {
                    CartView cartView = tuple.getT1();
                    BalanceResponse balance = tuple.getT2();

                    // сохраняем баланс в сессии
                    session.getAttributes().put("currentBalance", balance.getBalance());

                    model.addAttribute("cart", cartView);
                    model.addAttribute("currentBalance", balance.getBalance());

                    return Mono.just("cart");
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

    @GetMapping("/prepare-payment")
    @ResponseBody
    public Mono<Map<String, Object>> preparePayment(
            @CookieValue(name = "CART_SESSION", required = false) String sessionId) {
        if (sessionId == null) {
            return Mono.just(Map.of("balance", 0));
        }

        return cartService.getCartView(sessionId)
                .flatMap(cartView -> paymentServiceClient.getBalance()
                        .map(balance -> Map.of(
                                "balance", balance.getBalance(),
                                "totalAmount", cartView.getTotalAmount())));
    }

    @PostMapping("/confirm-payment")
    public Mono<String> confirmPayment(
            @CookieValue(name = "CART_SESSION", required = false) String sessionId,
            ServerWebExchange exchange,
            Model model) {

        if (sessionId == null) {
            return Mono.just("redirect:/products");
        }

        return cartService.getCartView(sessionId)
                .flatMap(cartView -> {
                    double totalAmount = cartView.getTotalAmount().doubleValue();

                    return cartService.checkout(sessionId)
                            .flatMap(order -> {
                                PaymentRequest request = new PaymentRequest();
                                request.setOrderId(order.getId().toString());
                                request.setAmount(totalAmount);
                                request.setCurrency("RUB");
                                request.setMethod("CARD");

                                return paymentServiceClient.pay(request)
                                        .flatMap(paymentResponse ->
                                // В любом случае достаём баланс
                                paymentServiceClient.getBalance()
                                        .flatMap(balance -> {
                                            model.addAttribute("currentBalance", balance.getBalance());
                                            if (PaymentResponse.StatusEnum.SUCCESS
                                                    .equals(paymentResponse.getStatus())) {

                                                ConfirmRequest confirmRequest = new ConfirmRequest();
                                                confirmRequest.setOrderId(order.getId().toString());
                                                confirmRequest.setTransactionId(paymentResponse.getTransactionId());

                                                return paymentServiceClient.confirm(confirmRequest)
                                                        .flatMap(confirmResponse -> {
                                                            if (Boolean.TRUE.equals(confirmResponse.getConfirmed())) {
                                                                // очищаем CART_SESSION
                                                                exchange.getResponse()
                                                                        .addCookie(ResponseCookie
                                                                                .from("CART_SESSION", "")
                                                                                .path("/")
                                                                                .maxAge(0)
                                                                                .build());
                                                                return Mono.just("redirect:/orders/" + order.getId());
                                                            } else {
                                                                model.addAttribute("errorMessage",
                                                                        "Ошибка подтверждения заказа");
                                                                model.addAttribute("cart", cartView);
                                                                return Mono.just("cart");
                                                            }
                                                        });
                                            } else {
                                                model.addAttribute("errorMessage", paymentResponse.getMessage());
                                                model.addAttribute("cart", cartView);
                                                return Mono.just("cart");
                                            }
                                        }));
                            });
                });
    }
}
