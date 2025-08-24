package example.toyshop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import example.toyshop.model.CartItem;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface CartItemRepository extends R2dbcRepository<CartItem, Long> {
    Flux<CartItem> findByCartId(Long cartId);
    Mono<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
