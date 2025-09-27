package example.toyshop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {

    @Id
    private Long id;

    private String username;

    private String password; // уже зашифрованный

    private boolean enabled = true;

    private String email; // новое поле
}

    // роли в отдельной таблице user_roles
    // R2DBC не поддерживает @ElementCollection, поэтому роли надо вынести в отдельную сущность
    // и связать через UserRoleRepository

