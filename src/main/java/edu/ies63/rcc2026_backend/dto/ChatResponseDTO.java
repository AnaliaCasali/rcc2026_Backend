package edu.ies63.rcc2026_backend.dto;


import java.time.LocalDateTime;

public record ChatResponseDTO(
        String conversacionId,
        String respuesta,
        LocalDateTime fecha
) {}

