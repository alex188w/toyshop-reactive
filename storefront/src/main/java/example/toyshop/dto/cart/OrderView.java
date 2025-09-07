package example.toyshop.dto.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import example.toyshop.model.Cart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * DTO для отображения информации о заказе.
 * <p>
 * Содержит идентификатор, дату создания, список товаров и итоговую сумму заказа.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderView {
    private Long id;
    private LocalDateTime createdAt;
    private List<CartItemView> items;
    private BigDecimal totalAmount;
    private BigDecimal currentBalance; // новое поле

    public OrderView(Cart cart, List<CartItemView> items) {
        this.id = cart.getId();
        this.createdAt = cart.getCreatedAt();
        this.items = items;
        this.totalAmount = items.stream()
                .map(CartItemView::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.currentBalance = BigDecimal.ZERO; // по умолчанию
    }

    // Геттеры и сеттеры
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }
}
