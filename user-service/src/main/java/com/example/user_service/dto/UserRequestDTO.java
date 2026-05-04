package com.example.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    @NotEmpty(message = "User name can not be empty.")
    private String userName;
    @Email
    private String email;
    @NotBlank(message = "Password can not be empty.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain one upper, small, one number and special symbol"
    )
    private String password;

    @NotBlank(message = "phone number can not be empty.")
    @Pattern(
            regexp = "^(\\+994|0)(50|51|55|70|77)[0-9]{7}$",
            message = "phone number must start with +994 or 0 and contain valid operator code"
    )
    private String phoneNumber;
}
