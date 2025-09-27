package example.toyshop.config;

import example.toyshop.service.UserService;
import example.toyshop.model.User;
import example.toyshop.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        return userService.findByUsernameWithRoles(username)
                .flatMap(userWithRoles -> {
                    User user = userWithRoles.user();

                    // Проверяем пароль
                    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                        return Mono.empty();
                    }

                    // Преобразуем роли UserRole -> SimpleGrantedAuthority
                    List<GrantedAuthority> authorities = userWithRoles.roles().stream()
                            .map(UserRole::getRole) // берём строку из поля role
                            .map(SimpleGrantedAuthority::new) // создаём GrantedAuthority
                            .collect(Collectors.toList());

                    return Mono.just(
                            new UsernamePasswordAuthenticationToken(
                                    user.getUsername(), // ✅ principal = строка
                                    user.getPassword(),
                                    authorities

                            ));
                });
    }
}
