package example.toyshop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("user_roles") // таблица связей user_id - role
public class UserRole {

    @Id
    private Long id;

    private Long userId;

    private String role; // ROLE_USER, ROLE_ADMIN
}
