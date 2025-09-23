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
                                .authorizeExchange(exchange -> exchange
                                                .pathMatchers("/", "/info", "/help").permitAll()
                                                .anyExchange().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .authenticationSuccessHandler(
                                                                new RedirectServerAuthenticationSuccessHandler(
                                                                                "/products")))
                                .logout(logout -> logout.logoutSuccessHandler(
                                                oidcLogoutSuccessHandler(clientRegistrationRepository)))
                                .build();
        }

        private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(
                        ReactiveClientRegistrationRepository clientRegistrationRepository) {
                OidcClientInitiatedServerLogoutSuccessHandler successHandler = new OidcClientInitiatedServerLogoutSuccessHandler(
                                clientRegistrationRepository);

                // Куда редиректить после успешного logout в Keycloak
                successHandler.setPostLogoutRedirectUri("{baseUrl}/login");
                return successHandler;
        }
}


