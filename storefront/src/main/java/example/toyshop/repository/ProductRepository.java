package example.toyshop.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import example.toyshop.model.Product;
import reactor.core.publisher.Flux;

/**
 * Репозиторий для работы с товарами {@link Product}.
 * <p>
 * Расширяет {@link ReactiveCrudRepository} для асинхронного взаимодействия с базой данных через R2DBC.
 */
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

    /**
     * Находит товары, название которых содержит указанную подстроку, игнорируя регистр.
     *
     * @param q подстрока для поиска
     * @return поток товаров, удовлетворяющих условию
     */
    Flux<Product> findByNameContainingIgnoreCase(String q);
}
