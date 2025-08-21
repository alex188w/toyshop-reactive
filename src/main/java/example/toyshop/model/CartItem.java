package example.toyshop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("cart_item")
public class CartItem {
    @Id
    private Long id;

    @Column("cart_id")
    private Long cartId;

    @Column("product_id")
    private Long productId;

    private Integer quantity;
}
