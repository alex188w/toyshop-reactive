package example.toyshop.model;

import lombok.Data;

import java.math.BigDecimal;

import org.springframework.http.codec.multipart.FilePart;

@Data
public class ProductForm {
    private String name;
    private String description;
    private Integer price;
    private Integer quantity;
    private FilePart file; // WebFlux вместо MultipartFile
}
