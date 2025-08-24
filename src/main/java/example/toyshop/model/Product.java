package example.toyshop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("product")
public class Product {
    @Id
    private Long id; // в БД BIGSERIAL
    private String name;
    private String description;
    private Integer price; // в копейках/центах — привычно и безопасно
    @Column("image_url")
    private String imageUrl;
    private Integer quantity;
}
