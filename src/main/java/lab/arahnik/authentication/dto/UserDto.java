package lab.arahnik.authentication.dto;

import lab.arahnik.authentication.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;
    private String username;
    private Role role;
}
