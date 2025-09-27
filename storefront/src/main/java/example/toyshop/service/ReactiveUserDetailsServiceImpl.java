package example.toyshop.service;

import example.toyshop.repository.UserRepository;
import example.toyshop.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .flatMap(user ->
                        userRoleRepository.findByUserId(user.getId())
                                .map(userRole -> new SimpleGrantedAuthority(userRole.getRole()))
                                .collectList()
                                .map(authorities -> {
                                    if (authorities.isEmpty()) {
                                        authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
                                    }
                                    return org.springframework.security.core.userdetails.User
                                            .withUsername(user.getUsername())
                                            .password(user.getPassword())
                                            .authorities(authorities)
                                            .accountExpired(false)
                                            .accountLocked(false)
                                            .credentialsExpired(false)
                                            .disabled(!user.isEnabled())
                                            .build();
                                })
                );
    }
}
