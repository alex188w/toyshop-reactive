package example.toyshop.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@TestConfiguration
@EnableWebFluxSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // Отключаем CSRF и разрешаем все запросы для тестов
        http.authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
            .csrf(csrf -> csrf.disable()); // новый стиль отключения CSRF

        return http.build();
    }
}
