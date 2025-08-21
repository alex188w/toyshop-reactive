package example.toyshop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Table("cart")
public class Cart {
    @Id
    private Long id;

    @Column("session_id")
    private String sessionId;

    private CartStatus status = CartStatus.ACTIVE;

    @Column("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
