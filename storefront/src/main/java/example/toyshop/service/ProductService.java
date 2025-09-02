package example.toyshop.service;

import lombok.RequiredArgsConstructor;

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

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;

    /**
     * Получить все товары.
     *
     * @return Flux всех товаров.
     */
    public Flux<Product> getAll() {
        return repo.findAll();
    }

    /**
     * Поиск товаров по части имени (без учёта регистра).
     *
     * @param q подстрока для поиска
     * @return Flux найденных товаров
     */
    public Flux<Product> search(String q) {
        return repo.findByNameContainingIgnoreCase(q);
    }

    /**
     * Сохранить загруженное изображение в папку uploads.
     *
     * @param file загруженный файл (FilePart WebFlux)
     * @return путь для сохранения в сущности Product
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
     * <p>
     * Транзакция нужна, если метод изменяет данные в базе.
     *
     * @param product товар для сохранения
     * @return Mono сохранённого товара
     */
    @Transactional
    public Mono<Product> save(Product product) {
        return repo.save(product);
    }

    /**
     * Получить товар по идентификатору.
     *
     * @param id идентификатор товара
     * @return Mono с товаром или пустым, если не найден
     */
    public Mono<Product> getById(Long id) {
        return repo.findById(id);
    }
}
