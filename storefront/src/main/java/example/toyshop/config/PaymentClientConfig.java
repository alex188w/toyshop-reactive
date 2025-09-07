package example.toyshop.config;

import com.example.openapi.client.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentClientConfig {
    @Bean
    public ApiClient paymentApiClient() {
        ApiClient client = new ApiClient();
        client.setBasePath("http://localhost:8081"); // важно!
        return client;
    }
}
