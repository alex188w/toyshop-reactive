package example.paymentService.controller;

import com.example.openapi.client.model.BalanceResponse;
import com.example.openapi.client.model.PaymentRequest;
import com.example.openapi.client.model.PaymentResponse;
import example.paymentService.service.BalanceService;
import com.example.openapi.client.model.ConfirmRequest;
import com.example.openapi.client.model.ConfirmResponse;
import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final BalanceService balanceService;

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @PostMapping("/pay")
    public Mono<PaymentResponse> pay(@RequestBody PaymentRequest request) {
        log.info("PAY request amount={}", request.getAmount());
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(UUID.randomUUID().toString());

        try {
            boolean success = balanceService.deduct(request.getAmount());
            if (success) {
                response.setStatus(PaymentResponse.StatusEnum.SUCCESS);
                response.setMessage("Оплата успешно проведена");
            } else {
                response.setStatus(PaymentResponse.StatusEnum.FAILED);
                response.setMessage("Недостаточно средств на балансе");
            }
        } catch (Exception e) {
            response.setStatus(PaymentResponse.StatusEnum.FAILED);
            response.setMessage("Ошибка при списании: " + e.getMessage());
        }

        return Mono.just(response);
    }

    @PostMapping("/confirm")
    public Mono<ConfirmResponse> confirm(@RequestBody ConfirmRequest request) {
        ConfirmResponse response = new ConfirmResponse();
        response.setOrderId(request.getOrderId());
        response.setTransactionId(request.getTransactionId());
        response.setConfirmed(true);
        response.setMessage("Заказ успешно оформлен");
        return Mono.just(response);
    }

    @GetMapping("/balance")
    public Mono<BalanceResponse> getBalance() {
        BalanceResponse response = new BalanceResponse();
        response.setBalance(balanceService.getBalance());
        response.setCurrency("RUB");
        return Mono.just(response);
    }
}
