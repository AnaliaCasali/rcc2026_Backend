package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Localidad;

public record OrganizadorResponseDTO(
    Long id,
    String username,
    Role role,
    String nombre,
    String biografia,
    String director,
    Localidad localidad
) {
}
