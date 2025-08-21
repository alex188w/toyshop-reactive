package example.toyshop.controller;

import example.toyshop.dto.cart.CartView;
import example.toyshop.model.Product;
import example.toyshop.model.ProductForm;
import example.toyshop.service.CartService;
import example.toyshop.service.ProductService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;
    private final CartService cartService;

    @GetMapping
    public Mono<String> listProducts(@CookieValue("JSESSIONID") String sessionId, Model model) {
        Mono<List<Product>> productsMono = service.getAll().collectList();
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

                    return "products";
                });
    }

    @GetMapping("/add")
    public String addForm() {
        return "add-product";
    }

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

    @GetMapping("/{id}")
    public Mono<String> getProduct(@PathVariable Long id, Model model) {
        return service.getById(id)
                .map(product -> {
                    model.addAttribute("product", product);
                    return "product"; // product.html
                });
    }
}
