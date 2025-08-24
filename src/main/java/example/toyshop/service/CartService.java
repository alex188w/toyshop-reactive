package example.toyshop.service;

import example.toyshop.dto.cart.CartItemView;
import example.toyshop.dto.cart.CartView;
import example.toyshop.model.Cart;
import example.toyshop.model.CartItem;
import example.toyshop.model.CartStatus;
import example.toyshop.repository.CartItemRepository;
import example.toyshop.repository.CartRepository;
import example.toyshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    /**
     * Получить или создать активную корзину для сессии
     */
    public Mono<Cart> getActiveCart(String sessionId) {
        return cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
                .switchIfEmpty(Mono.defer(() -> {
                    Cart cart = new Cart();
                    cart.setSessionId(sessionId);
                    cart.setStatus(CartStatus.ACTIVE);
                    return cartRepository.save(cart);
                }));
    }

    /**
     * Найти активную корзину без создания
     */
    public Mono<Cart> findActiveCart(String sessionId) {
        return cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
    }

    /**
     * Добавить товар в корзину (создаёт корзину при необходимости)
     */
    @Transactional
    public Mono<Void> addProduct(String sessionId, Long productId) {
        return getActiveCart(sessionId)
                .flatMap(cart -> cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                        .switchIfEmpty(Mono.defer(() -> {
                            CartItem newItem = new CartItem(null, cart.getId(), productId, 0);
                            return cartItemRepository.save(newItem);
                        }))
                        .flatMap(item -> productRepository.findById(productId)
                                .flatMap(product -> {
                                    if (item.getQuantity() < product.getQuantity()) {
                                        item.setQuantity(item.getQuantity() + 1);
                                        return cartItemRepository.save(item);
                                    }
                                    return Mono.just(item); // уже максимум
                                })))
                .then();
    }

    /**
     * Уменьшить количество товара в корзине (без создания корзины)
     */
    @Transactional
    public Mono<Void> decreaseProduct(String sessionId, Long productId) {
        return findActiveCart(sessionId)
                .flatMap(cart -> cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                        .flatMap(item -> {
                            if (item.getQuantity() > 1) {
                                item.setQuantity(item.getQuantity() - 1);
                                return cartItemRepository.save(item).then();
                            } else {
                                return cartItemRepository.delete(item);
                            }
                        }))
                .then();
    }

    /**
     * Увеличить количество товара в корзине (без создания корзины, с проверкой
     * склада)
     */
    @Transactional
    public Mono<Void> increaseProduct(String sessionId, Long productId) {
        return findActiveCart(sessionId)
                .flatMap(cart -> cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                        .flatMap(item -> productRepository.findById(productId)
                                .flatMap(product -> {
                                    if (item.getQuantity() < product.getQuantity()) {
                                        item.setQuantity(item.getQuantity() + 1);
                                        return cartItemRepository.save(item).then();
                                    }
                                    return Mono.empty(); // на складе больше нет
                                })))
                .then();
    }

    /**
     * Удалить товар из корзины полностью (без создания корзины)
     */
    @Transactional
    public Mono<Void> removeProduct(String sessionId, Long productId) {
        return findActiveCart(sessionId)
                .flatMap(cart -> cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                        .flatMap(cartItemRepository::delete))
                .then();
    }

    /**
     * Получить DTO для шаблона
     */
    public Mono<CartView> getCartView(String sessionId) {
        return findActiveCart(sessionId) // вместо getActiveCart
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(item -> productRepository.findById(item.getProductId())
                                .map(product -> new CartItemView(item, product)))
                        .collectList()
                        .map(items -> {
                            BigDecimal total = items.stream()
                                    .map(CartItemView::getTotalPrice)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            return new CartView(items, total);
                        }))
                .switchIfEmpty(Mono.just(new CartView(List.of(), BigDecimal.ZERO)));
    }

    @Transactional
    public Mono<Cart> checkout(String sessionId) {
        return cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
                .switchIfEmpty(Mono.error(new IllegalStateException("Активная корзина не найдена")))
                .doOnNext(cart -> System.out.println("[CHECKOUT] Найдена активная корзина id=" + cart.getId()))
                .flatMap(cart -> {
                    cart.setStatus(CartStatus.COMPLETED);
                    System.out.println("[CHECKOUT] Меняем статус корзины id=" + cart.getId() + " → COMPLETED");
                    return cartRepository.save(cart)
                            .doOnNext(saved -> System.out.println("[CHECKOUT] Сохранена корзина id=" + saved.getId() + " со статусом " + saved.getStatus()));
                });
    }
}
