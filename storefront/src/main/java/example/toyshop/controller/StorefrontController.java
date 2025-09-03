package example.toyshop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.openapi.client.model.BalanceResponse;

import example.toyshop.service.PaymentServiceClient;
import reactor.core.publisher.Mono;

@RestController
public class StorefrontController {

    private final PaymentServiceClient paymentServiceClient;

    public StorefrontController(PaymentServiceClient paymentServiceClient) {
        this.paymentServiceClient = paymentServiceClient;
    }

    @GetMapping("/my-balance")
    public Mono<BalanceResponse> myBalance() {
        return paymentServiceClient.getBalance();
    }
}
