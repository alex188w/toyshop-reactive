package example.toyshop.service;

import com.example.openapi.client.ApiClient;
import com.example.openapi.client.api.BalanceApi;
import com.example.openapi.client.model.BalanceResponse;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PaymentServiceClient {

    private final BalanceApi balanceApi;

    public PaymentServiceClient(ApiClient apiClient) {
        this.balanceApi = new BalanceApi(apiClient);
    }

    public Mono<BalanceResponse> getBalance() {
        return balanceApi.balanceGet()
                .onErrorResume(e -> Mono.error(new RuntimeException("Ошибка вызова paymentService", e)));
    }
}
