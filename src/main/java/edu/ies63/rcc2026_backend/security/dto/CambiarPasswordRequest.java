package edu.ies63.rcc2026_backend.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordRequest (
    @NotBlank(message = "La contraseña actual es obligatoria")
    String currentPassword,

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    String newPassword,

    @NotBlank(message = "La confirmación de contraseña es obligatoria")
    String confirmationPassword
) {}