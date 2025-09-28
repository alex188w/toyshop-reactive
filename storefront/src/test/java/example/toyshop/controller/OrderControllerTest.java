package example.toyshop.controller;

import example.toyshop.dto.cart.OrderView;
import example.toyshop.model.Cart;
import example.toyshop.model.CartItem;
import example.toyshop.model.CartStatus;
import example.toyshop.model.Product;
import example.toyshop.model.User;
import example.toyshop.repository.CartItemRepository;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import example.toyshop.service.PaymentServiceClient;
import example.toyshop.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import com.example.openapi.client.model.BalanceResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private UserService userService;

    private OrderController orderController;

    private Cart testCart;
    private CartItem testItem;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testProduct = new Product(2L, "Мяч", "Футбольный", 500, "img", 10);
        testItem = new CartItem(1L, 1L, 2L, 2);
        testCart = new Cart(1L, "1", CartStatus.COMPLETED, LocalDateTime.now());

        when(paymentServiceClient.getBalance())
                .thenReturn(Mono.just(new BalanceResponse().balance(BigDecimal.valueOf(1000))));

        orderController = new OrderController(cartRepository, cartItemRepository, productRepository,
                paymentServiceClient, userService);

        // Мокаем пользователя
        User user = new User(1L, "admin", null, true, "admin@test.com");
        UserService.UserWithRoles userWithRoles = new UserService.UserWithRoles(user, List.of());
        when(userService.findByUsernameWithRoles(toString()))
                .thenReturn(Mono.just(userWithRoles));
    }

    @Test
    void viewOrders_shouldReturnOrdersViewWithData() {
        when(cartRepository.findByStatusAndUserId(CartStatus.COMPLETED, "1"))
                .thenReturn(Flux.just(testCart));
        when(cartItemRepository.findByCartId(1L))
                .thenReturn(Flux.just(testItem));
        when(productRepository.findById(2L))
                .thenReturn(Mono.just(testProduct));

        Model model = new ConcurrentModel();

        // Не используем block(), вызываем прямо
        orderController.viewOrders(model).subscribe(viewName -> {
            assertThat(viewName).isEqualTo("orders");

            List<OrderView> orders = (List<OrderView>) model.getAttribute("orders");
            assertThat(orders).hasSize(1);
            assertThat(orders.get(0).getItems()).hasSize(1);
            assertThat(orders.get(0).getTotalAmount()).isEqualTo(BigDecimal.valueOf(1000));
        });
    }

    @Test
    void viewOrder_shouldReturnSingleOrderView() {
        when(cartRepository.findById(1L)).thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(Flux.just(testItem));
        when(productRepository.findById(2L)).thenReturn(Mono.just(testProduct));
        when(paymentServiceClient.getBalance()).thenReturn(Mono.just(new BalanceResponse().balance(BigDecimal.valueOf(1000))));

        Model model = new ConcurrentModel();

        orderController.viewOrder(1L, null, model).subscribe(viewName -> {
            assertThat(viewName).isEqualTo("order");

            OrderView order = (OrderView) model.getAttribute("order");
            assertThat(order).isNotNull();
            assertThat(order.getItems()).hasSize(1);
            assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.valueOf(1000));

            assertThat(model.getAttribute("currentBalance")).isEqualTo(BigDecimal.valueOf(1000));
        });
    }
}
