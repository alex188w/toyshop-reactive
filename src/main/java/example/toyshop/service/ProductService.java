package example.toyshop.service;

import lombok.RequiredArgsConstructor;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

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

    public Flux<Product> getAll() {
        return repo.findAll();
    }

    public Flux<Product> search(String q) {
        return repo.findByNameContainingIgnoreCase(q);
    }

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

    public Mono<Product> save(Product product) {
        return repo.save(product);
    }

        public Mono<Product> getById(Long id) {
        return repo.findById(id);
    }
}
