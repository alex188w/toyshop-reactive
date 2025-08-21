package example.toyshop.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class CartView {
    private List<CartItemView> items;
    private BigDecimal totalAmount;

    public CartView() {
        this.items = List.of();
        this.totalAmount = BigDecimal.ZERO;
    }
}

