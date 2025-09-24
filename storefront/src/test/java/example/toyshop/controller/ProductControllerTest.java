package example.toyshop.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.reactive.ServerHttpResponse;
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

// @ExtendWith(MockitoExtension.class)
// class ProductControllerTest {
//     private ProductService productService;
//     private CartService cartService;
//     private ProductController controller;
//     private Model model;
//     private ServerHttpResponse response = Mockito.mock(ServerHttpResponse.class);

//     @BeforeEach
//     void setup() {
//         productService = Mockito.mock(ProductService.class);
//         cartService = Mockito.mock(CartService.class);
//         model = Mockito.mock(Model.class);

//         controller = new ProductController(productService, cartService);
//     }

//     @Test
//     void listProducts_shouldAddProductsAndCartToModel() {
//         String sessionId = null;

//         Product p1 = new Product();
//         p1.setName("Мишка");
//         p1.setPrice(1000);

//         CartView cart = new CartView(List.of(), BigDecimal.ZERO);

//         Mockito.when(productService.getAll()).thenReturn(Flux.just(p1));
//         Mockito.when(cartService.getCartView(Mockito.anyString())).thenReturn(Mono.just(cart));

//         Mono<String> result = controller.listProducts(sessionId, response, null,
//                 "name_asc", 10, model);

//         StepVerifier.create(result)
//                 .expectNext("products")
//                 .verifyComplete();

//         Mockito.verify(model).addAttribute("products", List.of(p1));
//         Mockito.verify(model).addAttribute("cartTotalQuantity", 0);
//     }

//     @Test
//     void addForm_shouldReturnAddProductPage() {
//         String page = controller.addForm();
//         assertEquals("add-product", page);
//     }

//     @Test
//     void addProduct_shouldSaveProductAndRedirect() {
//         ProductForm form = new ProductForm();
//         form.setName("Лего");
//         form.setDescription("Конструктор");
//         form.setPrice(2000);
//         form.setQuantity(5);
//         form.setFile(null); // без изображения

//         Mockito.when(productService.save(Mockito.any(Product.class)))
//                 .thenReturn(Mono.just(new Product()));

//         Mono<String> result = controller.addProduct(form);

//         StepVerifier.create(result)
//                 .expectNext("redirect:/products")
//                 .verifyComplete();

//         Mockito.verify(productService).save(Mockito.any(Product.class));
//     }

//     @Test
//     void getProduct_shouldAddProductToModel() {
//         Product product = new Product();
//         product.setId(1L);
//         product.setName("Мяч");

//         Mockito.when(productService.getById(1L)).thenReturn(Mono.just(product));

//         Mono<String> result = controller.getProduct(1L, model);

//         StepVerifier.create(result)
//                 .expectNext("product")
//                 .verifyComplete();

//         Mockito.verify(model).addAttribute("product", product);
//     }
// }
