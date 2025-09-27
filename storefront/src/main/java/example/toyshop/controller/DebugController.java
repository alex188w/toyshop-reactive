package example.toyshop.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Возвращает информацию о текущем пользователе из контекста безопасности.
 * <p>
 * Метод проверяет {@link ReactiveSecurityContextHolder} и извлекает объект {@link Authentication}.
 * Формирует JSON с ключами:
 * <ul>
 *     <li>{@code username} — имя пользователя (или "Гость", если не аутентифицирован)</li>
 *     <li>{@code authenticated} — {@code true}, если пользователь аутентифицирован</li>
 *     <li>{@code authorities} — список ролей пользователя в виде строк, например {@code ["ROLE_ADMIN", "ROLE_USER"]}</li>
 * </ul>
 * <p>
 * Если контекст отсутствует или пользователь не аутентифицирован, возвращается объект по умолчанию:
 * <pre>{@code
 * {
 *     "username": "Гость",
 *     "authenticated": false,
 *     "authorities": []
 * }
 * }</pre>
 *
 * @return {@link Mono} с {@link Map} содержащей имя пользователя, флаг аутентификации и список ролей
 */
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    @GetMapping("/roles")
    public Mono<Map<String, Object>> getRoles() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .map(auth -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("username", auth.getName());
                    map.put("authenticated", auth.isAuthenticated());
                    map.put("authorities", auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList());
                    return map;
                })
                .defaultIfEmpty(Map.of(
                        "username", "Гость",
                        "authenticated", false,
                        "authorities", List.of()
                ));
    }
}
