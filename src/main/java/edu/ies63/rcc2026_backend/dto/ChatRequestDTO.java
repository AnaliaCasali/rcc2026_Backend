package edu.ies63.rcc2026_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

    public record ChatRequestDTO(
            @NotBlank(message = "El mensaje no puede estar vacío")
            @Size(max = 1000, message = "Máximo 1000 caracteres")
            String mensaje,
            String conversacionId   // opcional: null = conversación nueva
    ) {}
