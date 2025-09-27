package example.toyshop.controller;

import example.toyshop.model.User;
import example.toyshop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // Страница логина и регистрации (один шаблон auth.html)
    @GetMapping({"/login", "/auth"})
    public String authPage(@RequestParam(required = false) String action, Model model) {
        model.addAttribute("action", action != null ? action : "login");
        return "auth";
    }

    // POST регистрация
    @PostMapping("/signup")
    @ResponseBody
    public Mono<String> signup(@RequestBody User user) {
        return userService.existsByUsername(user.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just("Пользователь с таким именем уже существует");
                    } else {
                        return userService.register(user)
                                .then(Mono.just("Пользователь успешно зарегистрирован"));
                    }
                });
    }
}
