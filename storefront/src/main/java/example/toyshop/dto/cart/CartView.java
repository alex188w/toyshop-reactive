package example.toyshop.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO для отображения корзины пользователя.
 * <p>
 * Содержит список товаров и итоговую сумму.
 */
@Data
@AllArgsConstructor
public class CartView {

    /** Список элементов корзины */
    private List<CartItemView> items;

    /** Общая сумма всех товаров */
    private BigDecimal totalAmount;

    /**
     * Конструктор по умолчанию.
     * <p>
     * Создаёт пустую корзину с нулевой суммой.
     */
    public CartView() {
        this.items = List.of();
        this.totalAmount = BigDecimal.ZERO;
    }
}

