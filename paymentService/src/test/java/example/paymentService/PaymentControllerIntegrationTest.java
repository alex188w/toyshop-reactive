package example.paymentService;

import example.paymentService.service.BalanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.openapi.client.model.BalanceResponse;
import com.example.openapi.client.model.ConfirmRequest;
import com.example.openapi.client.model.ConfirmResponse;
import com.example.openapi.client.model.PaymentRequest;
import com.example.openapi.client.model.PaymentResponse;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
class PaymentControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BalanceService balanceService;

    private PaymentRequest paymentRequest;
    private ConfirmRequest confirmRequest;

    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId("order-1");
        paymentRequest.setAmount(Double.valueOf(500));
        paymentRequest.setCurrency("RUB");
        paymentRequest.setMethod("CARD");

        confirmRequest = new ConfirmRequest();
        confirmRequest.setOrderId("order-1");
        confirmRequest.setTransactionId(UUID.randomUUID().toString());
    }

    @Test
    void pay_shouldReturnSuccess_whenBalanceSufficient() {
        when(balanceService.deduct(paymentRequest.getAmount())).thenReturn(true);

        webTestClient.post()
                .uri("/pay")
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .value(resp -> {
                    assertThat(resp.getStatus()).isEqualTo(PaymentResponse.StatusEnum.SUCCESS);
                    assertThat(resp.getMessage()).isEqualTo("Оплата успешно проведена");
                    assertThat(resp.getTransactionId()).isNotNull();
                });
    }

    @Test
    void pay_shouldReturnFailed_whenBalanceInsufficient() {
        when(balanceService.deduct(paymentRequest.getAmount())).thenReturn(false);

        webTestClient.post()
                .uri("/pay")
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .value(resp -> {
                    assertThat(resp.getStatus()).isEqualTo(PaymentResponse.StatusEnum.FAILED);
                    assertThat(resp.getMessage()).isEqualTo("Недостаточно средств на балансе");
                    assertThat(resp.getTransactionId()).isNotNull();
                });
    }

    @Test
    void confirm_shouldReturnConfirmed() {
        webTestClient.post()
                .uri("/confirm")
                .bodyValue(confirmRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ConfirmResponse.class)
                .value(resp -> {
                    assertThat(resp.getConfirmed()).isTrue();
                    assertThat(resp.getOrderId()).isEqualTo(confirmRequest.getOrderId());
                    assertThat(resp.getTransactionId()).isEqualTo(confirmRequest.getTransactionId());
                    assertThat(resp.getMessage()).isEqualTo("Заказ успешно оформлен");
                });
    }

    @Test
    void getBalance_shouldReturnCurrentBalance() {
        when(balanceService.getBalance()).thenReturn(1234.56);

        webTestClient.get()
                .uri("/balance")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceResponse.class)
                .value(resp -> {
                    assertThat(resp.getBalance()).isEqualTo(1234.56);
                    assertThat(resp.getCurrency()).isEqualTo("RUB");
                });
    }
}
