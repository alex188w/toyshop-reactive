package example.toyshop.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.assertj.core.api.Assertions.assertThat;
import java.io.IOException;

import com.example.openapi.client.ApiClient;
import com.example.openapi.client.model.BalanceResponse;
import com.example.openapi.client.model.ConfirmRequest;
import com.example.openapi.client.model.ConfirmResponse;
import com.example.openapi.client.model.PaymentRequest;
import com.example.openapi.client.model.PaymentResponse;

import org.junit.jupiter.api.*;

// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
// class PaymentServiceClientIntegrationTest {

//     private MockWebServer mockWebServer;
//     private PaymentServiceClient paymentServiceClient;

//     @BeforeAll
//     void setupServer() throws IOException {
//         mockWebServer = new MockWebServer();
//         mockWebServer.start();

//         String baseUrl = mockWebServer.url("/").toString();

//         // Создаём WebClient с базовым URL mockWebServer
//         WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

//         // Передаём его в ApiClient, чтобы все API-запросы шли на MockWebServer
//         ApiClient apiClient = new ApiClient(webClient);
//         apiClient.setBasePath(baseUrl); // если ApiClient имеет setter basePath

//         paymentServiceClient = new PaymentServiceClient(apiClient);
//     }

//     @AfterAll
//     void shutdownServer() throws IOException {
//         mockWebServer.shutdown();
//     }

//     @Test
//     void getBalance_shouldReturnBalance() throws Exception {
//         // given
//         String responseBody = "{\"balance\":123.45}";
//         mockWebServer.enqueue(new MockResponse()
//                 .setBody(responseBody)
//                 .addHeader("Content-Type", "application/json"));

//         // when
//         Mono<BalanceResponse> result = paymentServiceClient.getBalance();

//         // then
//         StepVerifier.create(result)
//                 .expectNextMatches(balance -> balance.getBalance() == 123.45)
//                 .verifyComplete();

//         RecordedRequest recordedRequest = mockWebServer.takeRequest();
//         assertThat(recordedRequest.getPath()).isEqualTo("/balance");
//         assertThat(recordedRequest.getMethod()).isEqualTo("GET");
//     }

//     @Test
//     void pay_shouldReturnSuccessResponse() throws Exception {
//         // given
//         String responseBody = "{\"status\":\"SUCCESS\",\"transactionId\":\"txn-123\"}";
//         mockWebServer.enqueue(new MockResponse()
//                 .setBody(responseBody)
//                 .addHeader("Content-Type", "application/json"));

//         PaymentRequest request = new PaymentRequest()
//                 .orderId("1")
//                 .amount(100.0)
//                 .currency("RUB")
//                 .method("CARD");

//         // when
//         Mono<PaymentResponse> result = paymentServiceClient.pay(request);

//         // then
//         StepVerifier.create(result)
//                 .expectNextMatches(resp -> resp.getStatus() == PaymentResponse.StatusEnum.SUCCESS
//                         && "txn-123".equals(resp.getTransactionId()))
//                 .verifyComplete();

//         RecordedRequest recordedRequest = mockWebServer.takeRequest();
//         assertThat(recordedRequest.getPath()).isEqualTo("/pay");
//         assertThat(recordedRequest.getMethod()).isEqualTo("POST");
//     }

//     @Test
//     void confirm_shouldReturnConfirmed() throws Exception {
//         // given
//         String responseBody = "{\"confirmed\":true}";
//         mockWebServer.enqueue(new MockResponse()
//                 .setBody(responseBody)
//                 .addHeader("Content-Type", "application/json"));

//         ConfirmRequest request = new ConfirmRequest()
//                 .orderId("1")
//                 .transactionId("txn-123");

//         // when
//         Mono<ConfirmResponse> result = paymentServiceClient.confirm(request);

//         // then
//         StepVerifier.create(result)
//                 .expectNextMatches(ConfirmResponse::getConfirmed)
//                 .verifyComplete();

//         RecordedRequest recordedRequest = mockWebServer.takeRequest();
//         assertThat(recordedRequest.getPath()).isEqualTo("/confirm");
//         assertThat(recordedRequest.getMethod()).isEqualTo("POST");
//     }
// }
