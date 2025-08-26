package example.toyshop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import example.toyshop.model.CartItem;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

/**
 * Репозиторий для работы с элементами корзины {@link CartItem}.
 * <p>
 * Расширяет {@link R2dbcRepository} для асинхронного взаимодействия с базой данных через R2DBC.
 */
public interface CartItemRepository extends R2dbcRepository<CartItem, Long> {

    /**
     * Находит все элементы корзины по идентификатору корзины.
     *
     * @param cartId идентификатор корзины
     * @return поток {@link CartItem} для указанной корзины
     */
    Flux<CartItem> findByCartId(Long cartId);

    /**
     * Находит элемент корзины по идентификатору корзины и идентификатору товара.
     *
     * @param cartId    идентификатор корзины
     * @param productId идентификатор товара
     * @return элемент корзины, если найден
     */
    Mono<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
