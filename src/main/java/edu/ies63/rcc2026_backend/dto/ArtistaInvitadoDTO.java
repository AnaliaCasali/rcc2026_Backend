package edu.ies63.rcc2026_backend.dto;

public record ArtistaInvitadoDTO(
        Long idPerfil,
        String nombre,
        String username,
        Boolean confirmado
) {}