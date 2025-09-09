package example.toyshop.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import example.toyshop.model.Product;
import example.toyshop.repository.ProductRepository;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "product:";
    private static final String CACHE_ALL = CACHE_PREFIX + "all";
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    /**
     * Получить все товары.
     * <p>
     * Сначала пытаемся взять из Redis, если кэша нет —
     * берём из базы и кладём в Redis.
     */
    public Flux<Product> getAll() {
        return redisTemplate.opsForList().size(CACHE_ALL)
                .flatMapMany(size -> {
                    if (size != null && size > 0) {
                        return redisTemplate.opsForList()
                                .range(CACHE_ALL, 0, -1)
                                .cast(Product.class);
                    } else {
                        return repo.findAll()
                                .collectList()
                                .flatMapMany(products -> redisTemplate.opsForList()
                                        .rightPushAll(CACHE_ALL, products.toArray())
                                        .then(redisTemplate.expire(CACHE_ALL, TTL)) // TTL
                                        .thenMany(Flux.fromIterable(products)));
                    }
                });
    }

    /**
     * Поиск товаров по части имени (без учёта регистра).
     */
    public Flux<Product> search(String q) {
        return repo.findByNameContainingIgnoreCase(q);
    }

    /**
     * Сохранить загруженное изображение в папку uploads.
     */
    public Mono<String> saveImage(FilePart file) {
        if (file == null)
            return Mono.just("");

        String uploadDir = "uploads";
        new File(uploadDir).mkdirs();

        String filename = System.currentTimeMillis() + "-" + file.filename();
        Path path = Paths.get(uploadDir, filename);

        return file.transferTo(path)
                .then(Mono.just("/uploads/" + filename));
    }

    /**
     * Сохранить или обновить товар.
     * После сохранения сбрасываем кэш: сам товар и список всех товаров.
     */
    @Transactional
    public Mono<Product> save(Product product) {
        return repo.save(product)
                .flatMap(saved -> redisTemplate.delete(CACHE_PREFIX + saved.getId())
                        .then(redisTemplate.delete(CACHE_ALL))
                        .thenReturn(saved));
    }

    /**
     * Получить товар по идентификатору.
     * Сначала ищем в Redis, если нет — в базе и кладём в кэш.
     */
    public Mono<Product> getById(Long id) {
        String key = CACHE_PREFIX + id;

        return redisTemplate.opsForValue().get(key)
                .cast(Product.class)
                .doOnNext(p -> log.info("Из кеша: {}", p.getName()))
                .switchIfEmpty(
                        repo.findById(id)
                                .doOnNext(p -> log.info("Из БД: {}", p.getName()))
                                .flatMap(product -> redisTemplate.opsForValue()
                                        .set(key, product, Duration.ofMinutes(5))
                                        .thenReturn(product)));
    }
}
