package edu.ies63.rcc2026_backend.dto;

public record LocalidadMapResponseDTO(
    long id,
    String localidad,
    Double latitud,
    Double longitud
) {
}
