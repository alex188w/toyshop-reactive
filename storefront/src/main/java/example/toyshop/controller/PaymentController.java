package example.toyshop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.openapi.client.model.BalanceResponse;

import example.toyshop.service.PaymentServiceClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentServiceClient paymentServiceClient;

    @GetMapping("/balance")
    public Mono<BalanceResponse> myBalance() {
        return paymentServiceClient.getBalance();
    }
}
