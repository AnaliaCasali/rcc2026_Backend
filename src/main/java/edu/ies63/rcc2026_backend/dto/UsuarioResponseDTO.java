package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.Role;

public record UsuarioResponseDTO(
    Long id,
    String username,
    Role role,
    String nombre,
    String biografia,
    String director,
    Long localidadId
) {
}
