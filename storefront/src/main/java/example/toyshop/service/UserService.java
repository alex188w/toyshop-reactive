package example.toyshop.service;

import example.toyshop.model.User;
import example.toyshop.model.UserRole;
import example.toyshop.repository.UserRepository;
import example.toyshop.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    // Регистрация нового пользователя с ролью ROLE_USER
    public Mono<User> register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user)
                .flatMap(savedUser -> userRoleRepository.save(new UserRole(null, savedUser.getId(), "ROLE_USER"))
                        .thenReturn(savedUser));
    }

    // Проверка существования пользователя по username
    public Mono<Boolean> existsByUsername(String username) {
        return userRepository.findByUsername(username)
                .hasElement();
    }

    // Получение пользователя с ролями
    public Mono<UserWithRoles> findByUsernameWithRoles(String username) {
        return userRepository.findByUsername(username)
                .flatMap(user -> userRoleRepository.findByUserId(user.getId())
                        .collectList()
                        .map(roles -> new UserWithRoles(user, roles)));
    }

    // Вспомогательный класс для возврата пользователя с ролями
    public record UserWithRoles(User user, java.util.List<UserRole> roles) {
    }
}
