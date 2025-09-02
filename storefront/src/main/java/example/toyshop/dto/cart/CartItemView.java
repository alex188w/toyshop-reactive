package example.toyshop.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import example.toyshop.model.CartItem;
import example.toyshop.model.Product;

/**
 * DTO для отображения элемента корзины с данными о товаре.
 * <p>
 * Содержит объединённую информацию из {@link CartItem} и {@link Product}.
 */
@Data
@AllArgsConstructor
public class CartItemView {

    /** ID продукта */
    private Long productId;

    /** Название продукта */
    private String name;

    /** Ссылка на изображение */
    private String imageUrl;

    /** Количество единиц товара */
    private Integer quantity;

    /** Цена за единицу */
    private BigDecimal price;

    /** Общая сумма = price * quantity */
    private BigDecimal totalPrice;

    /**
     * Конструктор для создания {@link CartItemView} на основе корзины и продукта.
     *
     * @param item    элемент корзины
     * @param product товар
     */
    public CartItemView(CartItem item, Product product) {
        this.productId = product.getId();
        this.name = product.getName();
        this.imageUrl = product.getImageUrl();
        this.quantity = item.getQuantity() != null ? item.getQuantity() : 0;
        this.price = BigDecimal.valueOf(product.getPrice());
        this.totalPrice = this.price.multiply(BigDecimal.valueOf(this.quantity));
    }
}
