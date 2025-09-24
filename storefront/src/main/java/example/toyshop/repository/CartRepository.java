package example.toyshop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import example.toyshop.model.Cart;
import example.toyshop.model.CartStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Репозиторий для работы с корзинами {@link Cart}.
 * <p>
 * Расширяет {@link R2dbcRepository} для асинхронного взаимодействия с базой данных через R2DBC.
 */
public interface CartRepository extends R2dbcRepository<Cart, Long> {

    /**
     * Находит корзину по идентификатору сессии и статусу.
     *
     * @param sessionId идентификатор сессии пользователя
     * @param status    статус корзины ({@link CartStatus})
     * @return корзина, если найдена
     */
    Mono<Cart> findByUserIdAndStatus(String sessionId, CartStatus status);

    /**
     * Находит все корзины с указанным статусом.
     *
     * @param status статус корзины
     * @return поток корзин с указанным статусом
     */
    Flux<Cart> findByStatus(CartStatus status);
}
