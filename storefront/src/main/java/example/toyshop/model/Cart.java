package example.toyshop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;

import org.springframework.data.relational.core.mapping.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность корзины пользователя.
 * <p>
 * Хранится в таблице {@code cart}. 
 * Связывает товары с конкретной сессией пользователя и хранит статус заказа.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("cart")
public class Cart {

    /** Уникальный идентификатор корзины */
    @Id
    private Long id;

    /** Идентификатор сессии пользователя, используется для привязки корзины */
    @NotBlank(message = "userId обязателен")
    @Column("user_id")
    private String userId;

    /** Статус корзины: {@link CartStatus#ACTIVE} или {@link CartStatus#COMPLETED} */
    private CartStatus status = CartStatus.ACTIVE;

    /** Дата и время создания корзины */
    @Column("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
