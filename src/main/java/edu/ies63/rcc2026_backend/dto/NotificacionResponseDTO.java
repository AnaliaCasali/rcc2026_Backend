package edu.ies63.rcc2026_backend.dto;

import java.time.LocalDateTime;

public record NotificacionResponseDTO(
    Long id,
    String mensaje,
    boolean leida,
    LocalDateTime fechaCreacion,
    Long eventoId
) {}