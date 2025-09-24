package example.toyshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

@Configuration
@EnableWebFluxSecurity
// Активация поддержки аннотаций для методов
@EnableReactiveMethodSecurity
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain springSecurityFilterChain(
                        ServerHttpSecurity http,
                        ReactiveClientRegistrationRepository clientRegistrationRepository) {

                return http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable) // отключаем для WebFlux + OAuth2
                                .authorizeExchange(exchanges -> exchanges
                                                // публичные страницы
                                                .pathMatchers("/", "/login", "/products", "/css/**", "/js/**",
                                                                "/uploads/**", "/me")
                                                .permitAll()
                                                // доступ только админу
                                                .pathMatchers("/admin/**").hasRole("admin")
                                                // доступ только авторизованному пользователю
                                                .pathMatchers("/cart/**", "/orders/**", "/checkout/**").authenticated()
                                                // остальное — по умолчанию авторизация
                                                .anyExchange().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .authenticationSuccessHandler(
                                                                new RedirectServerAuthenticationSuccessHandler(
                                                                                "/products")))
                                .logout(logout -> logout.logoutSuccessHandler(
                                                oidcLogoutSuccessHandler(clientRegistrationRepository)))
                                .build();
        }

        @Bean
        public ServerLogoutSuccessHandler oidcLogoutSuccessHandler(
                        ReactiveClientRegistrationRepository clientRegistrationRepository) {

                OidcClientInitiatedServerLogoutSuccessHandler logoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(
                                clientRegistrationRepository);

                // Указываем куда редиректить после logout (гость)
                logoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/products");

                return logoutSuccessHandler;
        }
}
