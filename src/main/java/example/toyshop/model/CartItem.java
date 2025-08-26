package example.toyshop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.relational.core.mapping.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сущность элемента корзины.
 * <p>
 * Хранится в таблице {@code cart_item}. 
 * Связывает корзину с конкретным товаром и количеством.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("cart_item")
public class CartItem {

    /** Уникальный идентификатор элемента корзины */
    @Id
    private Long id;

    /** ID корзины, к которой принадлежит элемент */
    @Column("cart_id")
    @NotNull(message = "cartId обязателен")
    private Long cartId;

    /** ID товара */
    @Column("product_id")
    @NotNull(message = "productId обязателен")
    private Long productId;

    /** Количество товара в корзине */
    @NotNull(message = "Количество товара обязательно")
    @Min(value = 1, message = "Количество товара должно быть не меньше 1")
    private Integer quantity;
}
