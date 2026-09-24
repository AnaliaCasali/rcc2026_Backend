package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.TipoImagen;

public record ImagenPerfilResponseDTO(
    Long id,
    String url,
    String alt,
    TipoImagen tipoImagen
) {
}