package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.Pais;
import edu.ies63.rcc2026_backend.enums.Provincia;

public record LocalidadResponseDTO(
    Long id,
    String localidad,
    Provincia provincia,
    Pais pais,
    Double latitud,
    Double longitud
) {
}
