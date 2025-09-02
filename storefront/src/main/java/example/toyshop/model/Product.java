package example.toyshop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.data.relational.core.mapping.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сущность товара в магазине.
 * <p>
 * Хранится в таблице {@code product}.
 * Используется в витрине товаров, корзине и заказах.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("product")
public class Product {

    /** Уникальный идентификатор товара (BIGSERIAL в БД) */
    @Id
    private Long id;

    /** Название товара */
    @NotBlank(message = "Название товара обязательно")
    @Size(max = 255, message = "Название товара должно быть не длиннее 255 символов")
    private String name;

    /** Подробное описание товара */
    @Size(max = 5000, message = "Описание товара слишком длинное")
    private String description;

    /**
     * Цена товара в минимальных единицах (копейках/центах).
     * <p>
     * Храним в {@code Integer}, чтобы избежать ошибок округления с BigDecimal.
     */
    @NotNull(message = "Цена товара обязательна")
    @Min(value = 0, message = "Цена не может быть отрицательной")
    private Integer price;

    /** Ссылка на изображение товара */
    @Column("image_url")
    private String imageUrl;

    /** Количество товара на складе */
    @NotNull(message = "Количество товара обязательно")
    @Min(value = 0, message = "Количество не может быть отрицательным")
    private Integer quantity;
}
