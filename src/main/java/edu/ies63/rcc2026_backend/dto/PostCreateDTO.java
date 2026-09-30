package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.TipoPost;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record PostCreateDTO(
    @NotBlank(message = "La publicación debe tener un título")
    @Size(max = 50, message = "El título no puede superar los 50 caracteres")
    String titulo,

    @Size(max = 280, message = "La descripción no puede superar los 280 caracteres")
    String descripcion,

    @NotNull(message = "Debes indicar qué tipo de publicación es")
    TipoPost tipoPost,

    @URL(message = "El enlace del video debe ser una URL válida (ej: https://www.youtube.com/...)")
    String linkVideo,

    @NotNull(message = "Debes incluir al menos una imagen")
    List<MultipartFile> imagenes
) {
}
