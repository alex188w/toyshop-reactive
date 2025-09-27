package example.toyshop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebSession;

import example.toyshop.dto.cart.CartItemView;
import example.toyshop.dto.cart.OrderView;
import example.toyshop.model.CartStatus;
import example.toyshop.repository.CartItemRepository;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import example.toyshop.service.PaymentServiceClient;
import example.toyshop.service.UserService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

/**
 * Контроллер для управления заказами.
 * <p>
 * Отвечает за отображение истории заказов (все корзины со статусом
 * {@link CartStatus#COMPLETED})
 * и просмотр конкретного заказа по его идентификатору.
 */
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final UserService userService;

    /** Получаем текущего пользователя реактивно */
    private Mono<UserService.UserWithRoles> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .flatMap(auth -> userService.findByUsernameWithRoles(auth.getName()));
    }

    /** Список завершённых заказов текущего пользователя */
    @GetMapping
    public Mono<String> viewOrders(Model model) {
        return getCurrentUser()
                .flatMap(userWithRoles -> {
                    String userId = userWithRoles.user().getId().toString();
                    return cartRepository.findByStatusAndUserId(CartStatus.COMPLETED, userId)
                            .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                                    .flatMap(item -> productRepository.findById(item.getProductId())
                                            .map(product -> new CartItemView(item, product)))
                                    .collectList()
                                    .map(items -> new OrderView(cart, items)))
                            .collectList()
                            .map(orderViews -> {
                                model.addAttribute("orders", orderViews);
                                return "orders";
                            });
                });
    }

    /** Детали конкретного заказа для текущего пользователя */
    @GetMapping("/{id}")
    public Mono<String> viewOrder(@PathVariable Long id, WebSession session, Model model) {
        return getCurrentUser()
                .flatMap(userWithRoles -> {
                    String userId = userWithRoles.user().getId().toString();
                    return cartRepository.findById(id)
                            .filter(cart -> userId.equals(cart.getUserId())) // проверка владельца
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден")))
                            .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                                    .flatMap(ci -> productRepository.findById(ci.getProductId())
                                            .map(product -> new CartItemView(ci, product)))
                                    .collectList()
                                    .map(items -> new OrderView(cart, items)))
                            .flatMap(orderView -> paymentServiceClient.getBalance()
                                    .map(balance -> {
                                        model.addAttribute("order", orderView);
                                        model.addAttribute("currentBalance", balance.getBalance());
                                        session.getAttributes().put("currentBalance", balance.getBalance());
                                        return "order";
                                    }));
                });
    }
}
