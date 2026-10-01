package edu.ies63.rcc2026_backend.dto;

public record PerfilBusquedaDTO(
        Long id,
        String nombre,
        String rol,
        String biografia,
        String localidad,
        String fotoPerfil,
        Double distanciaKm
) {}