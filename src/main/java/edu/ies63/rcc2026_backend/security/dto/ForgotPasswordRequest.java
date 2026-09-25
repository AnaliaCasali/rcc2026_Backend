package edu.ies63.rcc2026_backend.security.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "El identificador (email o username) es obligatorio")
        String email
) {}