package example.toyshop.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import example.toyshop.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpMethod;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        @Bean
        public ReactiveAuthenticationManager authenticationManager(UserService userService,
                        PasswordEncoder passwordEncoder) {
                return new CustomReactiveAuthenticationManager(userService, passwordEncoder);
        }

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                        ReactiveAuthenticationManager authenticationManager) {
                return http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .authorizeExchange(exchanges -> exchanges
                                                .pathMatchers("/", "/products", "/login", "/signup", "/auth", "/css/**",
                                                                "/js/**", "/uploads/**", "/api/auth-info", "/api/**")
                                                .permitAll()
                                                .pathMatchers("/add").hasRole("ADMIN")
                                                .anyExchange().authenticated())
                                .authenticationManager(authenticationManager)
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .authenticationSuccessHandler((webFilterExchange, authentication) -> {
                                                        webFilterExchange.getExchange().getResponse()
                                                                        .setStatusCode(HttpStatus.SEE_OTHER);
                                                        webFilterExchange.getExchange().getResponse().getHeaders()
                                                                        .setLocation(URI.create("/products"));
                                                        return webFilterExchange.getExchange().getResponse()
                                                                        .setComplete();
                                                }))
                                .logout(logout -> logout
                                                .requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET,
                                                                "/logout")) // разрешаем GET
                                                .logoutSuccessHandler((webFilterExchange, authentication) -> {
                                                        webFilterExchange.getExchange().getResponse()
                                                                        .setStatusCode(HttpStatus.SEE_OTHER);
                                                        webFilterExchange.getExchange().getResponse().getHeaders()
                                                                        .setLocation(URI.create("/products"));
                                                        return webFilterExchange.getExchange().getResponse()
                                                                        .setComplete();
                                                }))
                                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                                .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
