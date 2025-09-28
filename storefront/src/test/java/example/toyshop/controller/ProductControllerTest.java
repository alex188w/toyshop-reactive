package example.toyshop.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.Model;

import example.toyshop.dto.cart.CartView;
import example.toyshop.model.Product;
import example.toyshop.model.ProductForm;
import example.toyshop.service.CartService;
import example.toyshop.service.ProductService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.junit.jupiter.api.BeforeEach;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private CartService cartService;

    @Mock
    private Model model;

    @InjectMocks
    private ProductController controller;

    private OidcUser oidcUser;

    @BeforeEach
    void setup() {
        oidcUser = Mockito.mock(OidcUser.class);
        lenient().when(oidcUser.getSubject()).thenReturn("admin");
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void listProducts_shouldAddProductsAndCartToModel() {
        // Подготовка продуктов
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Мишка");
        p1.setPrice(1000);

        // Подготовка корзины
        CartView cart = new CartView(List.of(), BigDecimal.ZERO);

        // Lenient стабы
        lenient().when(productService.getAll()).thenReturn(Flux.just(p1));
        lenient().when(cartService.getCartView("admin")).thenReturn(Mono.just(cart));

        // Вызов метода
        Mono<String> result = controller.listProducts(oidcUser, null, "name_asc", 10, model);

        StepVerifier.create(result)
                .expectNext("products")
                .verifyComplete();

        // Проверка модели
        verify(model).addAttribute("products", List.of(p1));
        verify(model).addAttribute("cartTotalQuantity", 0);
        verify(model).addAttribute("keyword", null);
        verify(model).addAttribute("sort", "name_asc");
        verify(model).addAttribute("size", 10);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void addProduct_shouldSaveProductAndRedirect() {
        ProductForm form = new ProductForm();
        form.setName("Лего");
        form.setDescription("Конструктор");
        form.setPrice(2000);
        form.setQuantity(5);
        form.setFile(null); // без изображения

        lenient().when(productService.save(Mockito.any(Product.class)))
                .thenReturn(Mono.just(new Product()));

        Mono<String> result = controller.addProduct(form);

        StepVerifier.create(result)
                .expectNext("redirect:/products")
                .verifyComplete();

        verify(productService).save(Mockito.any(Product.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getProduct_shouldAddProductToModel() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Мяч");

        lenient().when(productService.getById(1L)).thenReturn(Mono.just(product));

        Mono<String> result = controller.getProduct(1L, model);

        StepVerifier.create(result)
                .expectNext("product")
                .verifyComplete();

        verify(model).addAttribute("product", product);
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void addForm_shouldReturnAddProductPage() {
        StepVerifier.create(controller.addForm())
                .expectNext("add-product")
                .verifyComplete();
    }
}
