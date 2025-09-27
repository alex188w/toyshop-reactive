package example.toyshop.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import example.toyshop.model.UserRole;
import reactor.core.publisher.Flux;

public interface UserRoleRepository extends ReactiveCrudRepository<UserRole, Long> {
    Flux<UserRole> findByUserId(Long userId);
}
