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

import example.toyshop.config.ClientRegistrationLogger;

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

@Service
public class PaymentServiceClient {

    private final WebClient webClient;
    private final ReactiveOAuth2AuthorizedClientManager authorizedClientManager;
    private static final Logger log = LoggerFactory.getLogger(ClientRegistrationLogger.class);

    public PaymentServiceClient(WebClient.Builder webClientBuilder,
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
        this.authorizedClientManager = authorizedClientManager;
    }

    private Mono<String> getAccessToken() {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId("payment-client")
                .principal("payment-client")
                .build();

        return authorizedClientManager.authorize(request)
                .doOnNext(client -> log.info("Authorized client: {}", client))
                .flatMap(client -> {
                    if (client == null || client.getAccessToken() == null) {
                        return Mono.error(new IllegalStateException("Не удалось получить access token"));
                    }
                    return Mono.just(client.getAccessToken().getTokenValue());
                });
    }

    public Mono<BalanceResponse> getBalance() {
        return getAccessToken()
                .flatMap(token -> webClient.get()
                        .uri("/balance")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .bodyToMono(BalanceResponse.class));
    }

    public Mono<PaymentResponse> pay(PaymentRequest request) {
        return getAccessToken()
                .flatMap(token -> webClient.post()
                        .uri("/pay")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(PaymentResponse.class));
    }

    public Mono<ConfirmResponse> confirm(ConfirmRequest request) {
        return getAccessToken()
                .flatMap(token -> webClient.post()
                        .uri("/confirm")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(ConfirmResponse.class));
    }

}
