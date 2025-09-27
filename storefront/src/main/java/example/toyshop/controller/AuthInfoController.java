package example.toyshop.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class AuthInfoController {

    @GetMapping("/auth-info")
    public Mono<Map<String, Object>> currentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .map(auth -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("authenticated", auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()));
                    userInfo.put("username", auth.getName());
                    return userInfo;
                })
                .defaultIfEmpty(Map.of("authenticated", false, "username", "Гость"));
    }
}
