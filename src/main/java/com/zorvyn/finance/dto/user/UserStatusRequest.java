package com.zorvyn.finance.dto.user;

import com.zorvyn.finance.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;
}