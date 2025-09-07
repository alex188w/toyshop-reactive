package example.toyshop.service;

import com.example.openapi.client.ApiClient;
import com.example.openapi.client.api.BalanceApi;
import com.example.openapi.client.api.ConfirmApi;
import com.example.openapi.client.api.PaymentApi;
import com.example.openapi.client.model.BalanceResponse;
import com.example.openapi.client.model.ConfirmRequest;
import com.example.openapi.client.model.ConfirmResponse;
import com.example.openapi.client.model.PaymentRequest;
import com.example.openapi.client.model.PaymentResponse;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PaymentServiceClient {

    private final BalanceApi balanceApi;
    private final PaymentApi paymentApi; // для POST /pay
    private final ConfirmApi confirmApi; // для POST /confirm

    public PaymentServiceClient(ApiClient apiClient) {
        this.balanceApi = new BalanceApi(apiClient);
        this.paymentApi = new PaymentApi(apiClient);
        this.confirmApi = new ConfirmApi(apiClient);
    }

    public Mono<BalanceResponse> getBalance() {
        return balanceApi.balanceGet()
                .onErrorResume(e -> Mono.error(new RuntimeException("Ошибка вызова paymentService", e)));
    }

    public Mono<PaymentResponse> pay(PaymentRequest request) {
        return paymentApi.payPost(request)
                .doOnError(e -> {
                    System.err.println("Ошибка при вызове /pay: " + e.getMessage());
                    if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException wcre) {
                        System.err.println("Status code: " + wcre.getStatusCode());
                        System.err.println("Response body: " + wcre.getResponseBodyAsString());
                    }
                })
                .onErrorResume(e -> Mono.error(new RuntimeException("Ошибка оплаты", e)));
    }

    public Mono<ConfirmResponse> confirm(ConfirmRequest request) {
        return confirmApi.confirmPost(request)
                .onErrorResume(e -> Mono.error(new RuntimeException("Ошибка подтверждения", e)));
    }
}
