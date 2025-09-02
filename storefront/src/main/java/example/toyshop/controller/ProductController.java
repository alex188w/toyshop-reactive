package example.toyshop.controller;

import example.toyshop.dto.cart.CartView;
import example.toyshop.model.Product;
import example.toyshop.model.ProductForm;
import example.toyshop.service.CartService;
import example.toyshop.service.ProductService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseCookie;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
            @CookieValue(name = "CART_SESSION", required = false) String sessionId,
            ServerHttpResponse response,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sort", defaultValue = "name_asc") String sort,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model) {

        // Генерация sessionId, если нет
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            ResponseCookie cookie = ResponseCookie.from("CART_SESSION", sessionId)
                    .path("/")
                    .httpOnly(true)
                    .build();
            response.addCookie(cookie);
        }

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

        Mono<CartView> cartMono = cartService.getCartView(sessionId);

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

                    return "products";
                });
    }

    /**
     * Отображает HTML-форму для добавления нового товара.
     *
     * @return имя HTML-шаблона {@code add-product}
     */
    @GetMapping("/add")
    public String addForm() {
        return "add-product";
    }

    /**
     * Обрабатывает отправку формы добавления товара.
     * Если прикреплено изображение — сохраняет его, иначе задаёт пустую строку в
     * imageUrl.
     *
     * @param form форма с данными товара (название, описание, цена, количество,
     *             изображение)
     * @return перенаправление на страницу списка товаров
     */
    @PostMapping("/add")
    public Mono<String> addProduct(@ModelAttribute ProductForm form) {
        FilePart file = form.getFile();
        Mono<String> imageMono;

        if (file != null) {
            imageMono = service.saveImage(file);
        } else {
            imageMono = Mono.just("");
        }

        return imageMono.flatMap(url -> {
            Product product = new Product();
            product.setName(form.getName());
            product.setDescription(form.getDescription());
            product.setPrice(form.getPrice());
            product.setQuantity(form.getQuantity());
            product.setImageUrl(url);
            return service.save(product);
        }).thenReturn("redirect:/products");
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
