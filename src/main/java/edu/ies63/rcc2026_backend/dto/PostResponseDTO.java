package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.TipoPost;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponseDTO(
    Long id,
    // datos autor
    Long usuarioId,
    String username,
    String nombreUsuario,
    String fotoPerfilUsuario,

    // datos usuario
    String titulo,
    String descripcion,
    TipoPost tipoPost,
    LocalDateTime fechaCreacion,
    String linkVideo,
    List<ImagenPostDTO> imagenes
) {
}
