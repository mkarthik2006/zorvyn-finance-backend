package com.zorvyn.finance.dto.user;

import com.zorvyn.finance.entity.Role;
import com.zorvyn.finance.entity.UserStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotNull(message = "Role is required (ADMIN, ANALYST, or VIEWER)")
    
    private Role role;

    private UserStatus status;
}
