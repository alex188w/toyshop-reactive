package example.toyshop.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST-контроллер для получения информации о текущем пользователе.
 * <p>
 * Предоставляет endpoint /api/auth-info, который возвращает информацию о
 * текущей аутентификации:
 * имя пользователя и статус аутентификации.
 * </p>
 */
@RestController
@RequestMapping("/api")
public class AuthInfoController {

    /**
     * Возвращает информацию о текущем пользователе.
     * <p>
     * Если пользователь аутентифицирован, возвращается его имя и признак
     * аутентификации.
     * Если пользователь анонимный или не аутентифицирован, возвращается "Гость" и
     * {@code authenticated = false}.
     * </p>
     *
     * @return Mono, содержащий Map с ключами:
     *         <ul>
     *         <li>{@code "authenticated"} — {@code true}, если пользователь
     *         аутентифицирован, иначе {@code false}</li>
     *         <li>{@code "username"} — имя пользователя или {@code "Гость"} для
     *         анонимного пользователя</li>
     *         </ul>
     */
    @GetMapping("/auth-info")
    public Mono<Map<String, Object>> currentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .map(auth -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("authenticated",
                            auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()));
                    userInfo.put("username",
                            "anonymousUser".equals(auth.getName()) ? "Гость" : auth.getName());
                    return userInfo;
                })
                .defaultIfEmpty(Map.of("authenticated", false, "username", "Гость"));
    }
}
