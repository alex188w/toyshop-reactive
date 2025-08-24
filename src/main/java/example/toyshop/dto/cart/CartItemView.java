package example.toyshop.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import example.toyshop.model.CartItem;
import example.toyshop.model.Product;

@Data
@AllArgsConstructor
public class CartItemView {
    private Long productId;
    private String name;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice; // <-- поменяли с int на BigDecimal

    public CartItemView(CartItem item, Product product) {
        this.productId = product.getId();
        this.name = product.getName();
        this.imageUrl = product.getImageUrl();
        this.quantity = item.getQuantity();        
        this.price = BigDecimal.valueOf(product.getPrice()); 
        this.totalPrice = this.price.multiply(BigDecimal.valueOf(quantity));
    }
}
