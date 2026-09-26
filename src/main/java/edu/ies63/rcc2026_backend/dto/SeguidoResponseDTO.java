package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.Role;

public record SeguidoResponseDTO(
    Long id,
    Long usuarioId,
    String nombre,
    Role tipoUsuario,
    String fotoPerfilUrl,
    boolean seguido
) {}
