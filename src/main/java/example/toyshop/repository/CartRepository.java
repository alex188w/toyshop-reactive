package example.toyshop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import example.toyshop.model.Cart;
import example.toyshop.model.CartStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartRepository extends R2dbcRepository<Cart, Long> {
    Mono<Cart> findBySessionIdAndStatus(String sessionId, CartStatus status);

    Flux<Cart> findByStatus(CartStatus status);
}