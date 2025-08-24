package example.toyshop.dto.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import example.toyshop.model.Cart;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class OrderView {
    private Long id;
    private LocalDateTime createdAt;
    private List<CartItemView> items;
    private BigDecimal totalAmount;

    public OrderView(Cart cart, List<CartItemView> items) {
        this.id = cart.getId();
        this.createdAt = cart.getCreatedAt();
        this.items = items;
        this.totalAmount = items.stream()
                .map(CartItemView::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
