package example.toyshop.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import reactor.core.publisher.Mono;

import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
                return http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable) // у тебя уже отключён CSRF
                                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                                .authorizeExchange(exchanges -> exchanges
                                                .pathMatchers("/", "/products", "/login", "/api/auth-info", "/signup", "/auth", "/css/**",
                                                                "/js/**", "/images/**")
                                                .permitAll()
                                                .pathMatchers("/cart/**", "/orders/**", "/checkout/**").authenticated()
                                                .anyExchange().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .authenticationSuccessHandler((webFilterExchange, authentication) -> {
                                                        System.out.println("Успешная авторизация: "
                                                                        + authentication.getName());
                                                        webFilterExchange.getExchange().getResponse()
                                                                        .setStatusCode(HttpStatus.SEE_OTHER);
                                                        webFilterExchange.getExchange().getResponse().getHeaders()
                                                                        .setLocation(URI.create("/products"));
                                                        return webFilterExchange.getExchange().getResponse()
                                                                        .setComplete();
                                                }))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET,
                                                                "/logout"))
                                                .logoutSuccessHandler((webFilterExchange, authentication) -> {
                                                        return webFilterExchange.getExchange().getSession()
                                                                        .flatMap(webSession -> webSession.invalidate())
                                                                        .then(Mono.fromRunnable(() -> {
                                                                                webFilterExchange.getExchange()
                                                                                                .getResponse()
                                                                                                .setStatusCode(HttpStatus.SEE_OTHER);
                                                                                webFilterExchange.getExchange()
                                                                                                .getResponse()
                                                                                                .getHeaders()
                                                                                                .setLocation(URI.create(
                                                                                                                "/products"));
                                                                        }))
                                                                        .then(webFilterExchange.getExchange()
                                                                                        .getResponse().setComplete());
                                                }))
                                .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}