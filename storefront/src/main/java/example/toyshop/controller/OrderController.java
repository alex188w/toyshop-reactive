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
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
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

        /**
         * Отображает список всех завершённых заказов.
         * <p>
         * Для каждой корзины со статусом {@link CartStatus#COMPLETED} подтягиваются
         * связанные товары,
         * формируется список {@link OrderView}, который передаётся в шаблон.
         *
         * @param model модель MVC для передачи списка заказов в представление
         * @return имя HTML-шаблона {@code orders}
         */
        @GetMapping
        public Mono<String> viewOrders(Model model) {
                return cartRepository.findByStatus(CartStatus.COMPLETED)
                                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                                                .flatMap(item -> productRepository.findById(item.getProductId())
                                                                .map(product -> new CartItemView(item, product)))
                                                .collectList()
                                                .map(items -> new OrderView(cart, items)))
                                .collectList()
                                .map(orderViews -> {
                                        model.addAttribute("orders", orderViews);
                                        return "orders"; // orders.html
                                });
        }

        /**
         * Отображает детали конкретного заказа по его идентификатору.
         * <p>
         * Если заказ не найден — выбрасывается {@link ResponseStatusException} со
         * статусом 404.
         * В модель добавляется {@link OrderView}, содержащий корзину и список её
         * товаров.
         *
         * @param id    идентификатор заказа
         * @param model модель MVC для передачи данных в представление
         * @return имя HTML-шаблона {@code order}
         */
        @GetMapping("/{id}")
        public Mono<String> viewOrder(@PathVariable Long id,
                        WebSession session,
                        Model model) {
                return cartRepository.findById(id)
                                .switchIfEmpty(Mono.error(
                                                new ResponseStatusException(HttpStatus.NOT_FOUND, "Заказ не найден")))
                                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                                                .flatMap(ci -> productRepository.findById(ci.getProductId())
                                                                .map(product -> new CartItemView(ci, product)))
                                                .collectList()
                                                .map(items -> new OrderView(cart, items)))
                                .flatMap(orderView ->
                                // Всегда получаем актуальный баланс с paymentService
                                paymentServiceClient.getBalance()
                                                .map(balance -> {
                                                        model.addAttribute("order", orderView);
                                                        model.addAttribute("currentBalance", balance.getBalance());

                                                        // обновить сессию 
                                                        session.getAttributes().put("currentBalance",
                                                                        balance.getBalance());

                                                        return "order";
                                                }));
        }

}
