package example.toyshop.model;

import lombok.Data;
import org.springframework.http.codec.multipart.FilePart;

/**
 * DTO-форма для добавления или редактирования товара.
 * <p>
 * Используется в {@link example.ProductController} для приёма данных из HTML-формы.
 */
@Data
public class ProductForm {

    /** Название товара */
    private String name;

    /** Описание товара */
    private String description;

    /** Цена товара (в условных единицах, например в рублях) */
    private Integer price;

    /** Количество доступного товара на складе */
    private Integer quantity;

    /** Файл изображения товара (WebFlux использует {@link FilePart} вместо MultipartFile) */
    private FilePart file;
}
