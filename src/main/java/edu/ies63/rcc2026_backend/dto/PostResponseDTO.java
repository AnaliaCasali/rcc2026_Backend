package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.TipoPost;

import java.util.List;

public record PostResponseDTO(
    Long id,
    String titulo,
    String descripcion,
    TipoPost tipoPost,
    String linkVideo,
    List<ImagenPostDTO> imagenes
) {}
