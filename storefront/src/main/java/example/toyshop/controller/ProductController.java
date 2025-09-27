package example.toyshop.controller;

import example.toyshop.dto.cart.CartView;
import example.toyshop.model.Product;
import example.toyshop.model.ProductForm;
import example.toyshop.service.CartService;
import example.toyshop.service.ProductService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseCookie;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Контроллер для управления товарами в магазине.
 * <p>
 * Обрабатывает отображение списка товаров, просмотр конкретного товара
 * и добавление нового товара.
 * Также интегрируется с {@link CartService}, чтобы показывать количество
 * товаров в корзине.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;
    private final CartService cartService;

    /**
     * Отображает список товаров с возможностью поиска, сортировки и ограничения по
     * размеру выборки.
     * Также обрабатывает cookie для идентификации корзины покупателя.
     *
     * @param sessionId идентификатор сессии корзины (берётся из cookie, может быть
     *                  null)
     * @param response  HTTP-ответ (для установки cookie при первой загрузке)
     * @param keyword   ключевое слово для фильтрации по названию (опционально)
     * @param sort      порядок сортировки (name_asc, name_desc, price_asc,
     *                  price_desc)
     * @param size      количество товаров для отображения
     * @param model     модель MVC для передачи данных в шаблон
     * @return имя HTML-шаблона {@code products}
     */
    @GetMapping
    public Mono<String> listProducts(
            @AuthenticationPrincipal OidcUser oidcUser,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sort", defaultValue = "name_asc") String sort,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model) {

        // Определяем ID пользователя
        String userId = oidcUser != null ? oidcUser.getSubject() : "guest";

        // // Проверяем, является ли пользователь админом
        // boolean isAdmin = oidcUser != null &&
        // oidcUser.getAuthorities().stream()
        // .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Mono<List<Product>> productsMono = service.getAll()
                .filter(p -> keyword == null || p.getName().toLowerCase().contains(keyword.toLowerCase()))
                .sort((p1, p2) -> {
                    switch (sort) {
                        case "price_asc":
                            return p1.getPrice().compareTo(p2.getPrice());
                        case "price_desc":
                            return p2.getPrice().compareTo(p1.getPrice());
                        case "name_desc":
                            return p2.getName().compareToIgnoreCase(p1.getName());
                        case "name_asc":
                        default:
                            return p1.getName().compareToIgnoreCase(p2.getName());
                    }
                })
                .take(size)
                .collectList();

        Mono<CartView> cartMono = cartService.getCartView(userId);

        return Mono.zip(productsMono, cartMono)
                .map(tuple -> {
                    List<Product> products = tuple.getT1();
                    CartView cart = tuple.getT2();

                    int totalQuantity = cart.getItems().stream()
                            .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                            .sum();

                    model.addAttribute("products", products);
                    model.addAttribute("cartTotalQuantity", totalQuantity);
                    model.addAttribute("keyword", keyword);
                    model.addAttribute("sort", sort);
                    model.addAttribute("size", size);
                    // Передаём флаг в модель
                    // model.addAttribute("isAdmin", isAdmin);

                    return "products";
                });
    }

    /**
     * Форма добавления товара — доступна только администратору.
     */
    @GetMapping("/add")
    // @PreAuthorize("hasRole('ADMIN')") // проверка роли на уровне метода
    // @PreAuthorize("#user.username == alex.name or hasRole('ADMIN')")
    public Mono<String> addForm() {
        return Mono.just("add-product");
    }

    /**
     * Обработка отправки формы добавления товара.
     * Доступна только администратору.
     *
     * @param form форма с данными товара (название, описание, цена, количество,
     *             изображение)
     * @return перенаправление на страницу списка товаров
     */

    @PostMapping("/add")
    public Mono<String> addProduct(@ModelAttribute ProductForm form) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> {
                    System.out.println("Auth for POST /add: " + ctx.getAuthentication());
                    return ctx.getAuthentication();
                })
                .then(Mono.defer(() -> {
                    FilePart file = form.getFile();
                    Mono<String> imageMono = file != null ? service.saveImage(file) : Mono.just("");
                    return imageMono.flatMap(url -> {
                        Product product = new Product();
                        product.setName(form.getName());
                        product.setDescription(form.getDescription());
                        product.setPrice(form.getPrice());
                        product.setQuantity(form.getQuantity());
                        product.setImageUrl(url);
                        return service.save(product);
                    }).thenReturn("redirect:/products");
                }));
    }

    /**
     * Отображает страницу с деталями конкретного товара.
     *
     * @param id    идентификатор товара
     * @param model модель MVC для передачи данных в шаблон
     * @return имя HTML-шаблона {@code product}
     */
    @GetMapping("/{id}")
    public Mono<String> getProduct(@PathVariable Long id, Model model) {
        return service.getById(id)
                .map(product -> {
                    model.addAttribute("product", product);
                    return "product"; // product.html
                });
    }
}
