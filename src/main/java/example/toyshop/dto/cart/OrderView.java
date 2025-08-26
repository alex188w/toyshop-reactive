package example.toyshop.dto.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import example.toyshop.model.Cart;
import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * DTO для отображения информации о заказе.
 * <p>
 * Содержит идентификатор, дату создания, список товаров и итоговую сумму заказа.
 */
@Data
@AllArgsConstructor
public class OrderView {

    /** Идентификатор заказа */
    private Long id;

    /** Дата и время создания заказа */
    private LocalDateTime createdAt;

    /** Список товаров в заказе */
    private List<CartItemView> items;

    /** Общая сумма заказа */
    private BigDecimal totalAmount;

    /**
     * Конструктор, создающий {@link OrderView} на основе сущности корзины и списка её элементов.
     *
     * @param cart  корзина, преобразованная в заказ
     * @param items список элементов корзины с данными о товарах
     */
    public OrderView(Cart cart, List<CartItemView> items) {
        this.id = cart.getId();
        this.createdAt = cart.getCreatedAt();
        this.items = items;
        this.totalAmount = items.stream()
                .map(CartItemView::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
