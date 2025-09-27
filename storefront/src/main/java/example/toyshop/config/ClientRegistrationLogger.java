package example.toyshop.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class ClientRegistrationLogger {

    private static final Logger log = LoggerFactory.getLogger(ClientRegistrationLogger.class);
    private final ReactiveClientRegistrationRepository clientRegistrationRepository;

    @PostConstruct
    public void logRegistrations() {
        clientRegistrationRepository.findByRegistrationId("payment-client")
            .doOnNext(r -> log.info("Found client: {}", r.getClientId()))
            .switchIfEmpty(Mono.fromRunnable(() -> log.warn("payment-client not found")))
            .subscribe();
    }    
}
