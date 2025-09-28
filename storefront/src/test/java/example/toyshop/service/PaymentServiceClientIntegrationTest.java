package example.toyshop.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import com.example.openapi.client.model.ConfirmRequest;
import com.example.openapi.client.model.PaymentRequest;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import static org.mockito.Mockito.mock;
import org.springframework.security.oauth2.client.*;

/**
 * Интеграционный тест для {@link PaymentServiceClient}.
 * <p>
 * Тестирует основные методы клиента платежного сервиса:
 * {@link PaymentServiceClient#getBalance()},
 * {@link PaymentServiceClient#pay(com.example.openapi.client.model.PaymentRequest)},
 * {@link PaymentServiceClient#confirm(com.example.openapi.client.model.ConfirmRequest)}.
 * </p>
 * <p>
 * В отличие от стандартных Spring Boot тестов, данный класс не поднимает
 * весь ApplicationContext. Для этого используется отдельный тестовый
 * конструктор
 * {@link PaymentServiceClient#PaymentServiceClient(WebClient.Builder, ReactiveOAuth2AuthorizedClientManager)}
 * и {@link MockWebServer}, который имитирует ответы внешнего платежного
 * сервиса.
 * </p>
 * <p>
 * OAuth2 авторизация также мокается через
 * {@link ReactiveOAuth2AuthorizedClientManager},
 * чтобы тестировать работу клиента без реального OAuth2 сервера.
 * </p>
 * <p>
 * Благодаря этому подходу, тесты быстрые, изолированные и не зависят от других
 * бинов
 * Spring контекста.
 * </p>
 */

// class PaymentServiceClientIntegrationTest {

// private static MockWebServer mockWebServer;
// private PaymentServiceClient paymentServiceClient;

// @BeforeAll
// static void setupServer() throws IOException {
// mockWebServer = new MockWebServer();
// mockWebServer.start();
// }

// @AfterAll
// static void shutdownServer() throws IOException {
// mockWebServer.shutdown();
// }

// @BeforeEach
// void setupClient() {
// String baseUrl = mockWebServer.url("/").toString();
// WebClient.Builder webClientBuilder = WebClient.builder();

// // мок для authorizedClientManager
// ReactiveOAuth2AuthorizedClientManager manager =
// mock(ReactiveOAuth2AuthorizedClientManager.class);
// OAuth2AuthorizedClient client = mock(OAuth2AuthorizedClient.class);
// when(client.getAccessToken()).thenReturn(
// new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
// "token",
// Instant.now(),
// Instant.now().plusSeconds(3600)));
// when(manager.authorize(any())).thenReturn(Mono.just(client));

// // ⚡ используем конструктор с baseUrl
// paymentServiceClient = new PaymentServiceClient(webClientBuilder, manager,
// baseUrl);
// }

// @Test
// void getBalance_shouldReturnBalance() throws InterruptedException {
// mockWebServer.enqueue(new MockResponse()
// .setBody("{\"balance\":123.45}")
// .addHeader("Content-Type", "application/json"));

// StepVerifier.create(paymentServiceClient.getBalance())
// .expectNextMatches(balance ->
// balance.getBalance().compareTo(BigDecimal.valueOf(123.45)) == 0)
// .verifyComplete();

// RecordedRequest recordedRequest = mockWebServer.takeRequest();
// Assertions.assertEquals("/balance", recordedRequest.getPath());
// Assertions.assertEquals("GET", recordedRequest.getMethod());
// }

// @Test
// void pay_shouldReturnPaymentResponse() throws InterruptedException {
// mockWebServer.enqueue(new MockResponse()
// .setBody("{\"status\":\"SUCCESS\",\"transactionId\":\"tx123\"}")
// .addHeader("Content-Type", "application/json"));

// PaymentRequest request = new PaymentRequest();
// request.setAmount(BigDecimal.valueOf(500));
// request.setCurrency("USD");
// request.setMethod("card");

// StepVerifier.create(paymentServiceClient.pay(request))
// .expectNextMatches(response ->
// "SUCCESS".equals(response.getStatus().getValue()) &&
// "tx123".equals(response.getTransactionId()))
// .verifyComplete();

// RecordedRequest recordedRequest = mockWebServer.takeRequest();
// Assertions.assertEquals("/pay", recordedRequest.getPath());
// Assertions.assertEquals("POST", recordedRequest.getMethod());
// Assertions.assertEquals("Bearer token",
// recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
// }

// @Test
// void confirm_shouldReturnConfirmResponse() throws InterruptedException {
// mockWebServer.enqueue(new MockResponse()
// .setBody("{\"orderId\":\"1\",\"transactionId\":\"tx123\",\"confirmed\":true,\"message\":\"Оплата
// успешна\"}")
// .addHeader("Content-Type", "application/json"));

// ConfirmRequest request = new ConfirmRequest();
// request.setOrderId("1");
// request.setTransactionId("tx123");

// StepVerifier.create(paymentServiceClient.confirm(request))
// .expectNextMatches(r -> Boolean.TRUE.equals(r.getConfirmed())
// && "1".equals(r.getOrderId())
// && "tx123".equals(r.getTransactionId()))
// .verifyComplete();

// RecordedRequest recordedRequest = mockWebServer.takeRequest();
// Assertions.assertEquals("/confirm", recordedRequest.getPath());
// Assertions.assertEquals("POST", recordedRequest.getMethod());
// Assertions.assertEquals("Bearer token",
// recordedRequest.getHeader(HttpHeaders.AUTHORIZATION));
// }
// }
