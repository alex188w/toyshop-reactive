package example.toyshop.config;

import com.example.openapi.client.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class PaymentClientConfig {

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Bean
    public ApiClient paymentApiClient() {
        ApiClient client = new ApiClient();
        client.setBasePath(paymentServiceUrl); // берем из application-docker.yml
        return client;
    }
}
