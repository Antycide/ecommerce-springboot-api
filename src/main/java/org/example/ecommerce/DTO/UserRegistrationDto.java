package org.example.ecommerce.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record UserRegistrationDto(@NotBlank(message = "Username cannot be empty") String username,
                                  @NotBlank(message = "Email cannot be empty") @Email String email,
                                  @NotBlank(message = "Password cannot be empty") String password) {
}
