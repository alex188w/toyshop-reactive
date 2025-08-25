package example.toyshop.service;

import example.toyshop.model.Product;
import example.toyshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.nio.file.Path;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository repo;

    @Mock
    private FilePart filePart;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testProduct = new Product(1L, "Toy Car", "Red toy car", 10, null, null);
    }

    @Test
    void getAll_shouldReturnProducts() {
        when(repo.findAll()).thenReturn(Flux.just(testProduct));

        StepVerifier.create(productService.getAll())
                .expectNext(testProduct)
                .verifyComplete();

        verify(repo).findAll();
    }

    @Test
    void search_shouldReturnMatchingProducts() {
        when(repo.findByNameContainingIgnoreCase("car")).thenReturn(Flux.just(testProduct));

        StepVerifier.create(productService.search("car"))
                .expectNext(testProduct)
                .verifyComplete();

        verify(repo).findByNameContainingIgnoreCase("car");
    }

    @Test
    void save_shouldReturnSavedProduct() {
        when(repo.save(testProduct)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.save(testProduct))
                .expectNext(testProduct)
                .verifyComplete();

        verify(repo).save(testProduct);
    }

    @Test
    void getById_shouldReturnProductIfExists() {
        when(repo.findById(1L)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.getById(1L))
                .expectNext(testProduct)
                .verifyComplete();

        verify(repo).findById(1L);
    }

    @Test
    void saveImage_shouldReturnEmptyStringIfFileIsNull() {
        StepVerifier.create(productService.saveImage(null))
                .expectNext("")
                .verifyComplete();
    }

    @Test
    void saveImage_shouldSaveFileAndReturnUrl() {
        when(filePart.filename()).thenReturn("image.png");
        when(filePart.transferTo(any(Path.class))).thenReturn(Mono.empty());

        StepVerifier.create(productService.saveImage(filePart))
                .assertNext(url -> {
                    assert url.startsWith("/uploads/");
                    assert url.endsWith(".png");
                })
                .verifyComplete();

        verify(filePart).transferTo(any(Path.class));
    }
}
