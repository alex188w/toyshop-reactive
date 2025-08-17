package example.toyshop.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import example.toyshop.model.Product;
import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {
  Flux<Product> findByNameContainingIgnoreCase(String q);
}
