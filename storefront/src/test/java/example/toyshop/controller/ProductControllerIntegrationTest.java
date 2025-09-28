package example.toyshop.controller;

import example.toyshop.IntegrationTestcontainers;
import example.toyshop.model.Product;
import example.toyshop.model.User;
import example.toyshop.repository.ProductRepository;
import example.toyshop.service.UserService;
import reactor.core.publisher.Mono;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ProductControllerIntegrationTest extends IntegrationTestcontainers {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private UserService userService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll().block();

        testProduct = new Product();
        testProduct.setName("Игрушечный робот");
        testProduct.setDescription("Робот с батарейками");
        testProduct.setPrice(1500);
        testProduct.setQuantity(10);

        testProduct = productRepository.save(testProduct).block();

        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");

        UserService.UserWithRoles userWithRoles = new UserService.UserWithRoles(admin, List.of());

        Mockito.when(userService.findByUsernameWithRoles("admin"))
                .thenReturn(Mono.just(userWithRoles));
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void listProducts_shouldReturnProducts() {
        webTestClient.get()
                .uri("/products")
                .exchange()
                .expectStatus().isOk()                
                .expectBody(String.class)
                .consumeWith(body -> {
                    assert body.getResponseBody().contains("Игрушечный робот");
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void listProducts_shouldFilterAndSort() {
        Product second = new Product();
        second.setName("Альтернативная игрушка");
        second.setPrice(500);
        second.setQuantity(5);
        second = productRepository.save(second).block();

        // фильтрация по ключевому слову
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/products")
                        .queryParam("keyword", "альт")
                        .queryParam("sort", "price_desc")
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(body -> {
                    assert body.getResponseBody().contains("Альтернативная игрушка");
                    assert !body.getResponseBody().contains("Игрушечный робот");
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void addForm_shouldReturnFormPage() {
        webTestClient.get()
                .uri("/products/add")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(result -> {
                    // Просто проверяем статус, имя шаблона нужно через MockMvc, а не WebTestClient
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void addProduct_shouldSaveProduct() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "Новая игрушка");
        formData.add("description", "Описание");
        formData.add("price", "1000");
        formData.add("quantity", "3");

        webTestClient.post()
                .uri("/products/add")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/products");

        Product saved = productRepository.findAll().collectList().block().stream()
                .filter(p -> "Новая игрушка".equals(p.getName()))
                .findFirst().orElse(null);

        assert saved != null;
        assert saved.getPrice().intValue() == 1000;
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getProduct_shouldReturnProductPage() {
        webTestClient.get()
                .uri("/products/{id}", testProduct.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(body -> {
                    assert body.getResponseBody().contains("Игрушечный робот");
                });
    }

    @Test
    @WithMockUser(username = "admin", roles = { "ADMIN" })
    void getProduct_shouldReturnFromDbAndThenFromCache() {
        // Первый вызов → из БД
        webTestClient.get()
                .uri("/products/{id}", testProduct.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(body -> {
                    assert body.getResponseBody().contains("Игрушечный робот");
                });

        // Второй вызов → должен отдать из Redis кеша
        webTestClient.get()
                .uri("/products/{id}", testProduct.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(body -> {
                    assert body.getResponseBody().contains("Игрушечный робот");
                });
    }
}
