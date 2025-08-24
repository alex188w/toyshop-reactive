package example.toyshop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import example.toyshop.dto.cart.CartItemView;
import example.toyshop.dto.cart.OrderView;
import example.toyshop.model.CartStatus;
import example.toyshop.repository.CartItemRepository;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    /**
     * История заказов (все корзины со статусом COMPLETED).
     */
    @GetMapping
    public Mono<String> viewOrders(Model model) {
        return cartRepository.findByStatus(CartStatus.COMPLETED)
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(item -> productRepository.findById(item.getProductId())
                                .map(product -> new CartItemView(item, product)))
                        .collectList()
                        .map(items -> new OrderView(cart, items))
                )
                .collectList()
                .map(orderViews -> {
                    model.addAttribute("orders", orderViews);
                    return "orders"; // orders.html
                });
    }

    // @GetMapping("/{id}")
    // public String viewOrder(Model model) {
    //     return "order";
    // }

    @GetMapping("/{id}")
    public Mono<String> viewOrder(@PathVariable Long id, Model model) {
        return cartRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден")))
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(ci -> productRepository.findById(ci.getProductId())
                                .map(product -> new CartItemView(ci, product)))
                        .collectList()
                        .map(items -> new OrderView(cart, items))
                )
                .map(orderView -> {
                    model.addAttribute("order", orderView); // <-- OrderView кладём
                    return "order";
                });
    }
}
