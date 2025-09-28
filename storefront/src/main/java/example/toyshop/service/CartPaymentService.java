package example.toyshop.service;

import example.toyshop.dto.cart.CartView;
import example.toyshop.model.Cart;
import com.example.openapi.client.model.BalanceResponse;
import com.example.openapi.client.model.ConfirmRequest;
import com.example.openapi.client.model.PaymentRequest;
import com.example.openapi.client.model.PaymentResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CartPaymentService {

    private final CartService cartService;
    private final PaymentServiceClient paymentServiceClient;

    /**
     * Подготавливает данные корзины и баланса для отображения пользователю.
     */
    public Mono<Map<String, Object>> prepareCartForView(String userId) {
        Mono<CartView> cartMono = cartService.getCartView(userId);
        Mono<BalanceResponse> balanceMono = paymentServiceClient.getBalance()
                .onErrorReturn(new BalanceResponse().balance(BigDecimal.ZERO));

        return Mono.zip(cartMono, balanceMono)
                .map(tuple -> Map.of(
                        "cart", tuple.getT1(),
                        "currentBalance", tuple.getT2().getBalance()
                ));
    }

    /**
     * Обрабатывает checkout и оплату корзины.
     * Возвращает URL для redirect (успешно или с ошибкой).
     */
    public Mono<String> checkoutAndPay(String userId) {
        return cartService.getCartView(userId)
                .flatMap(cartView -> cartService.checkout(userId)
                        .flatMap(cart -> doPayment(cart, cartView)));
    }

    private Mono<String> doPayment(Cart cart, CartView cartView) {
        BigDecimal totalAmount = cartView.getTotalAmount();

        PaymentRequest request = new PaymentRequest();
        request.setOrderId(cart.getId().toString());
        request.setAmount(totalAmount);
        request.setCurrency("RUB");
        request.setMethod("CARD");

        return paymentServiceClient.pay(request)
                .flatMap(resp -> handlePaymentResponse(cart, cartView, resp));
    }

    private Mono<String> handlePaymentResponse(Cart cart, CartView cartView, PaymentResponse paymentResponse) {
        return paymentServiceClient.getBalance()
                .flatMap(balance -> {
                    if (PaymentResponse.StatusEnum.SUCCESS.equals(paymentResponse.getStatus())) {
                        ConfirmRequest confirmRequest = new ConfirmRequest();
                        confirmRequest.setOrderId(cart.getId().toString());
                        confirmRequest.setTransactionId(paymentResponse.getTransactionId());

                        return paymentServiceClient.confirm(confirmRequest)
                                .map(confirmResp -> Boolean.TRUE.equals(confirmResp.getConfirmed())
                                        ? "redirect:/orders/" + cart.getId()
                                        : "cart"); // можно добавить модель с ошибкой
                    } else {
                        return Mono.just("cart"); // можно добавить модель с paymentResponse.getMessage()
                    }
                });
    }
}
