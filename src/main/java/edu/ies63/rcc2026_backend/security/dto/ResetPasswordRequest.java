package edu.ies63.rcc2026_backend.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "El token es obligatorio")
        String token,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
        String newPassword,

        @NotBlank(message = "La confirmación de la contraseña es obligatoria")
        String confirmationPassword
) {}