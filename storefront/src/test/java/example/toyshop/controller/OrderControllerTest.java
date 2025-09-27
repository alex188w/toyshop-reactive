package example.toyshop.controller;

import example.toyshop.dto.cart.OrderView;
import example.toyshop.model.Cart;
import example.toyshop.model.CartItem;
import example.toyshop.model.CartStatus;
import example.toyshop.model.Product;
import example.toyshop.repository.CartItemRepository;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import example.toyshop.service.PaymentServiceClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.server.WebSession;

import com.example.openapi.client.model.BalanceResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderControllerTest {

    // @Mock
    // private CartRepository cartRepository;

    // @Mock
    // private CartItemRepository cartItemRepository;

    // @Mock
    // private ProductRepository productRepository;

    // @InjectMocks
    // private OrderController orderController;

    // private Cart testCart;
    // private CartItem testItem;
    // private Product testProduct;

    // @Mock
    // private PaymentServiceClient paymentServiceClient;

    // @BeforeEach
    // void setUp() {
    //     MockitoAnnotations.openMocks(this);

    //     testCart = new Cart(1L, "session1", CartStatus.COMPLETED, LocalDateTime.now());
    //     testItem = new CartItem(1L, 1L, 2L, 2); // productId=2
    //     testProduct = new Product(2L, "Мяч", "Футбольный", 500, "img", 10);

    //     when(paymentServiceClient.getBalance())
    //             .thenReturn(Mono.just(new BalanceResponse().balance(1000.0)));
    // }

    // @Test
    // void viewOrders_shouldReturnOrdersViewWithData() {
    //     when(cartRepository.findByStatus(CartStatus.COMPLETED)).thenReturn(Flux.just(testCart));
    //     when(cartItemRepository.findByCartId(1L)).thenReturn(Flux.just(testItem));
    //     when(productRepository.findById(2L)).thenReturn(Mono.just(testProduct));

    //     Model model = new ConcurrentModel();

    //     // вызываем метод с актуальной сигнатурой
    //     String viewName = orderController.viewOrders(model).block();

    //     assertThat(viewName).isEqualTo("orders");
    //     List<OrderView> orders = (List<OrderView>) model.getAttribute("orders");
    //     assertThat(orders).hasSize(1);
    //     assertThat(orders.get(0).getItems()).hasSize(1);
    //     assertThat(orders.get(0).getTotalAmount()).isEqualTo(BigDecimal.valueOf(1000)); // 2 * 500
    // }

    // @Test
    // void viewOrder_shouldReturnSingleOrderView() {
    //     when(cartRepository.findById(1L)).thenReturn(Mono.just(testCart));
    //     when(cartItemRepository.findByCartId(1L)).thenReturn(Flux.just(testItem));
    //     when(productRepository.findById(2L)).thenReturn(Mono.just(testProduct));

    //     Model model = new ConcurrentModel();

    //     // Мокаем WebSession
    //     WebSession session = mock(WebSession.class);
    //     Map<String, Object> attributes = new HashMap<>();
    //     when(session.getAttributes()).thenReturn(attributes);

    //     // Теперь вызываем метод
    //     String viewName = orderController.viewOrder(1L, session, model).block();

    //     assertThat(viewName).isEqualTo("order");
    //     OrderView order = (OrderView) model.getAttribute("order");
    //     assertThat(order).isNotNull();
    //     assertThat(order.getItems()).hasSize(1);
    //     assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.valueOf(1000));

    //     // Проверяем, что в сессии установился currentBalance
    //     assertThat(attributes.get("currentBalance")).isEqualTo(1000.0);
    // }
}
