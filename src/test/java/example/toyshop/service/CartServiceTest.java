package example.toyshop.service;

import example.toyshop.dto.cart.CartItemView;
import example.toyshop.dto.cart.CartView;
import example.toyshop.model.*;
import example.toyshop.repository.CartItemRepository;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private Cart testCart;
    private CartItem testItem;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testCart = new Cart(1L, "session1", CartStatus.ACTIVE, LocalDateTime.now());
        testItem = new CartItem(1L, 1L, 2L, 1); // cartId=1, productId=2, quantity=1
        testProduct = new Product(2L, "Мяч", "Футбольный", 500, "img", 10);
    }

    @Test
    void getActiveCart_shouldReturnExistingCart() {
        when(cartRepository.findBySessionIdAndStatus("session1", CartStatus.ACTIVE))
                .thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.getActiveCart("session1"))
                .expectNext(testCart)
                .verifyComplete();
    }

    @Test
    void getActiveCart_shouldCreateNewCartIfNotExists() {
        when(cartRepository.findBySessionIdAndStatus("session2", CartStatus.ACTIVE))
                .thenReturn(Mono.empty());
        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> {
                    Cart c = invocation.getArgument(0);
                    c.setId(99L);
                    return Mono.just(c);
                });

        StepVerifier.create(cartService.getActiveCart("session2"))
                .assertNext(cart -> {
                    assert cart.getId() == 99L;
                    assert cart.getSessionId().equals("session2");
                    assert cart.getStatus() == CartStatus.ACTIVE;
                })
                .verifyComplete();
    }

    @Test
    void addProduct_shouldIncreaseQuantityIfAvailable() {
        when(cartRepository.findBySessionIdAndStatus("session1", CartStatus.ACTIVE))
                .thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 2L))
                .thenReturn(Mono.just(testItem));
        when(productRepository.findById(2L)).thenReturn(Mono.just(testProduct));
        when(cartItemRepository.save(any(CartItem.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(cartService.addProduct("session1", 2L))
                .verifyComplete();

        verify(cartItemRepository).save(argThat(ci -> ci.getQuantity() == 2));
    }

    @Test
    void decreaseProduct_shouldRemoveItemIfQuantityIsOne() {
        when(cartRepository.findBySessionIdAndStatus("session1", CartStatus.ACTIVE))
                .thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 2L))
                .thenReturn(Mono.just(testItem));
        when(cartItemRepository.delete(testItem)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.decreaseProduct("session1", 2L))
                .verifyComplete();

        verify(cartItemRepository).delete(testItem);
    }

    @Test
    void increaseProduct_shouldIncreaseQuantityIfStockAvailable() {
        testItem.setQuantity(1);
        when(cartRepository.findBySessionIdAndStatus("session1", CartStatus.ACTIVE))
                .thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 2L))
                .thenReturn(Mono.just(testItem));
        when(productRepository.findById(2L)).thenReturn(Mono.just(testProduct));
        when(cartItemRepository.save(any(CartItem.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(cartService.increaseProduct("session1", 2L))
                .verifyComplete();

        verify(cartItemRepository).save(argThat(ci -> ci.getQuantity() == 2));
    }

    @Test
    void removeProduct_shouldDeleteItem() {
        when(cartRepository.findBySessionIdAndStatus("session1", CartStatus.ACTIVE))
                .thenReturn(Mono.just(testCart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 2L))
                .thenReturn(Mono.just(testItem));
        when(cartItemRepository.delete(testItem)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.removeProduct("session1", 2L))
                .verifyComplete();

        verify(cartItemRepository).delete(testItem);
    }

    public Mono<CartView> getCartView(String sessionId) {
        return cartService.findActiveCart(sessionId)
                .switchIfEmpty(cartRepository.save(new Cart(null, sessionId, CartStatus.ACTIVE, LocalDateTime.now())))
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(item -> productRepository.findById(item.getProductId())
                                .map(product -> new CartItemView(item, product)))
                        .collectList()
                        .map(items -> {
                            BigDecimal total = items.stream()
                                    .map(CartItemView::getTotalPrice)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            return new CartView(items, total);
                        }));
    }

    // @Test
    // void checkout_shouldSetCartStatusToCompleted() {
    // when(cartRepository.findBySessionIdAndStatus("session1", CartStatus.ACTIVE))
    // .thenReturn(Mono.just(testCart));
    // when(cartRepository.save(any(Cart.class)))
    // .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

    // StepVerifier.create(cartService.checkout("session1"))
    // .assertNext(cart -> assert cart.getStatus() == CartStatus.COMPLETED)
    // .verifyComplete();
    // }
}
