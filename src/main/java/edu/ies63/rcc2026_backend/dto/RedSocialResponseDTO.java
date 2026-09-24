package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.Plataforma;

public record RedSocialResponseDTO(
    Long id,
    Plataforma plataforma,
    String url,
    String nombreUsuario
) {
}