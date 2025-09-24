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
import java.time.LocalDateTime;

/**
 * Сервис для работы с корзиной пользователя.
 * <p>
 * Обеспечивает создание и получение активной корзины, добавление, удаление и
 * изменение количества
 * товаров, а также оформление заказа (checkout). Все операции выполняются
 * асинхронно через R2DBC.
 */

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    /**
     * Получить активную корзину по userId или создать новую, если её нет.
     *
     * @param userId уникальный идентификатор пользователя (sub из токена)
     * @return активная корзина
     */
    public Mono<Cart> getActiveCart(String userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .switchIfEmpty(Mono.defer(() -> {
                    Cart cart = new Cart();
                    cart.setUserId(userId);
                    cart.setStatus(CartStatus.ACTIVE);
                    cart.setCreatedAt(LocalDateTime.now());
                    return cartRepository.save(cart);
                }));
    }

    /**
     * Найти активную корзину без создания новой.
     *
     * @param userId идентификатор пользователя
     * @return активная корзина, если существует
     */
    public Mono<Cart> findActiveCart(String userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE);
    }

    /**
     * Добавить товар в корзину. Создаёт корзину при необходимости.
     *
     * @param userId    идентификатор пользователя
     * @param productId идентификатор товара
     * @return Mono<Void> после завершения операции
     */
    @Transactional
    public Mono<Void> addProduct(String userId, Long productId) {
        return getActiveCart(userId)
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
                                    return Mono.just(item); // максимум достигнут
                                })))
                .then();
    }

    /**
     * Уменьшить количество товара в корзине. Не создаёт новую корзину.
     *
     * @param userId    идентификатор пользователя
     * @param productId идентификатор товара
     * @return Mono<Void> после завершения операции
     */
    @Transactional
    public Mono<Void> decreaseProduct(String userId, Long productId) {
        return findActiveCart(userId)
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
     * Увеличить количество товара в корзине. Не создаёт новую корзину.
     * Проверяет наличие товара на складе.
     *
     * @param userId    идентификатор пользователя
     * @param productId идентификатор товара
     * @return Mono<Void> после завершения операции
     */
    @Transactional
    public Mono<Void> increaseProduct(String userId, Long productId) {
        return findActiveCart(userId)
                .flatMap(cart -> cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                        .flatMap(item -> productRepository.findById(productId)
                                .flatMap(product -> {
                                    if (item.getQuantity() < product.getQuantity()) {
                                        item.setQuantity(item.getQuantity() + 1);
                                        return cartItemRepository.save(item).then();
                                    }
                                    return Mono.empty(); // товара на складе больше нет
                                })))
                .then();
    }

    /**
     * Полностью удалить товар из корзины.
     *
     * @param userId    идентификатор пользователя
     * @param productId идентификатор товара
     * @return Mono<Void> после завершения операции
     */
    @Transactional
    public Mono<Void> removeProduct(String userId, Long productId) {
        return findActiveCart(userId)
                .flatMap(cart -> cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                        .flatMap(cartItemRepository::delete))
                .then();
    }

    /**
     * Получить представление корзины для отображения пользователю.
     *
     * @param userId идентификатор пользователя
     * @return {@link CartView} с элементами корзины и общей суммой
     */
    public Mono<CartView> getCartView(String userId) {
        return findActiveCart(userId)
                .switchIfEmpty(cartRepository.save(new Cart(null, userId, CartStatus.ACTIVE, LocalDateTime.now())))
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

    /**
     * Оформление заказа (checkout) — перевод корзины в статус COMPLETED.
     *
     * @param userId идентификатор пользователя
     * @return обновлённая корзина со статусом COMPLETED
     */
    @Transactional
    public Mono<Cart> checkout(String userId) {
        return cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .switchIfEmpty(Mono.error(new IllegalStateException("Активная корзина не найдена")))
                .doOnNext(cart -> System.out.println("[CHECKOUT] Найдена активная корзина id=" + cart.getId()))
                .flatMap(cart -> {
                    cart.setStatus(CartStatus.COMPLETED);
                    System.out.println("[CHECKOUT] Меняем статус корзины id=" + cart.getId() + " → COMPLETED");
                    return cartRepository.save(cart)
                            .doOnNext(saved -> System.out.println("[CHECKOUT] Сохранена корзина id=" + saved.getId()
                                    + " со статусом " + saved.getStatus()));
                });
    }
}


// @Service
// @RequiredArgsConstructor
// public class CartService {

//     private final CartRepository cartRepository;
//     private final CartItemRepository cartItemRepository;
//     private final ProductRepository productRepository;

//     /**
//      * Получить активную корзину по sessionId или создать новую, если её нет.
//      *
//      * @param sessionId идентификатор сессии пользователя
//      * @return активная корзина
//      */
//     public Mono<Cart> getActiveCart(String sessionId) {
//         return cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
//                 .switchIfEmpty(Mono.defer(() -> {
//                     Cart cart = new Cart();
//                     cart.setSessionId(sessionId);
//                     cart.setStatus(CartStatus.ACTIVE);
//                     return cartRepository.save(cart);
//                 }));
//     }

//     /**
//      * Найти активную корзину без создания новой.
//      *
//      * @param sessionId идентификатор сессии пользователя
//      * @return активная корзина, если существует
//      */
//     public Mono<Cart> findActiveCart(String sessionId) {
//         return cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
//     }

//     /**
//      * Добавить товар в корзину. Создаёт корзину при необходимости.
//      *
//      * @param sessionId идентификатор сессии пользователя
//      * @param productId идентификатор товара
//      * @return Mono<Void> после завершения операции
//      */
//     @Transactional
//     public Mono<Void> addProduct(String sessionId, Long productId) {
//         return getActiveCart(sessionId)
//                 .flatMap(cart -> cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
//                         .switchIfEmpty(Mono.defer(() -> {
//                             CartItem newItem = new CartItem(null, cart.getId(), productId, 0);
//                             return cartItemRepository.save(newItem);
//                         }))
//                         .flatMap(item -> productRepository.findById(productId)
//                                 .flatMap(product -> {
//                                     if (item.getQuantity() < product.getQuantity()) {
//                                         item.setQuantity(item.getQuantity() + 1);
//                                         return cartItemRepository.save(item);
//                                     }
//                                     return Mono.just(item); // максимум достигнут
//                                 })))
//                 .then();
//     }

//     /**
//      * Уменьшить количество товара в корзине. Не создаёт новую корзину.
//      *
//      * @param sessionId идентификатор сессии пользователя
//      * @param productId идентификатор товара
//      * @return Mono<Void> после завершения операции
//      */
//     @Transactional
//     public Mono<Void> decreaseProduct(String sessionId, Long productId) {
//         return findActiveCart(sessionId)
//                 .flatMap(cart -> cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
//                         .flatMap(item -> {
//                             if (item.getQuantity() > 1) {
//                                 item.setQuantity(item.getQuantity() - 1);
//                                 return cartItemRepository.save(item).then();
//                             } else {
//                                 return cartItemRepository.delete(item);
//                             }
//                         }))
//                 .then();
//     }

//     /**
//      * Увеличить количество товара в корзине. Не создаёт новую корзину.
//      * Проверяет наличие товара на складе.
//      *
//      * @param sessionId идентификатор сессии пользователя
//      * @param productId идентификатор товара
//      * @return Mono<Void> после завершения операции
//      */
//     @Transactional
//     public Mono<Void> increaseProduct(String sessionId, Long productId) {
//         return findActiveCart(sessionId)
//                 .flatMap(cart -> cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
//                         .flatMap(item -> productRepository.findById(productId)
//                                 .flatMap(product -> {
//                                     if (item.getQuantity() < product.getQuantity()) {
//                                         item.setQuantity(item.getQuantity() + 1);
//                                         return cartItemRepository.save(item).then();
//                                     }
//                                     return Mono.empty(); // товара на складе больше нет
//                                 })))
//                 .then();
//     }

//     /**
//      * Полностью удалить товар из корзины.
//      *
//      * @param sessionId идентификатор сессии пользователя
//      * @param productId идентификатор товара
//      * @return Mono<Void> после завершения операции
//      */
//     @Transactional
//     public Mono<Void> removeProduct(String sessionId, Long productId) {
//         return findActiveCart(sessionId)
//                 .flatMap(cart -> cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
//                         .flatMap(cartItemRepository::delete))
//                 .then();
//     }

//     /**
//      * Получить представление корзины для отображения пользователю.
//      *
//      * @param sessionId идентификатор сессии пользователя
//      * @return {@link CartView} с элементами корзины и общей суммой
//      */
//     public Mono<CartView> getCartView(String sessionId) {
//         return findActiveCart(sessionId)
//                 .switchIfEmpty(cartRepository.save(new Cart(null, sessionId, CartStatus.ACTIVE, LocalDateTime.now())))
//                 .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
//                         .flatMap(item -> productRepository.findById(item.getProductId())
//                                 .map(product -> new CartItemView(item, product)))
//                         .collectList()
//                         .map(items -> {
//                             BigDecimal total = items.stream()
//                                     .map(CartItemView::getTotalPrice)
//                                     .reduce(BigDecimal.ZERO, BigDecimal::add);
//                             return new CartView(items, total);
//                         }));
//     }

//     /**
//      * Оформление заказа (checkout) — перевод корзины в статус COMPLETED.
//      *
//      * @param sessionId идентификатор сессии пользователя
//      * @return обновлённая корзина со статусом COMPLETED
//      */
//     @Transactional
//     public Mono<Cart> checkout(String sessionId) {
//         return cartRepository.findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE)
//                 .switchIfEmpty(Mono.error(new IllegalStateException("Активная корзина не найдена")))
//                 .doOnNext(cart -> System.out.println("[CHECKOUT] Найдена активная корзина id=" + cart.getId()))
//                 .flatMap(cart -> {
//                     cart.setStatus(CartStatus.COMPLETED);
//                     System.out.println("[CHECKOUT] Меняем статус корзины id=" + cart.getId() + " → COMPLETED");
//                     return cartRepository.save(cart)
//                             .doOnNext(saved -> System.out.println("[CHECKOUT] Сохранена корзина id=" + saved.getId()
//                                     + " со статусом " + saved.getStatus()));
//                 });
//     }
// }
