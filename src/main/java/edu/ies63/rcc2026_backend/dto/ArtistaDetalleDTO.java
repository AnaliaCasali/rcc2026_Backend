package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.EstadoInvitacion;

public record ArtistaDetalleDTO(
        Long invitacionId,
        Long artistaId,
        String nombre,
        String fotoPerfil,
        EstadoInvitacion estado
) {}