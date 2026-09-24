package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.Role;

import java.util.List;

public record PerfilResponseDTO(
    Long id,
    String username,
    Role role,
    String nombre,
    String biografia,
    String director,
    Long localidadId,
    String localidadStr,
    String whatsapp,
    List<ImagenPerfilResponseDTO> imagenesPerfil,
    List<RedSocialResponseDTO> redes
) {}