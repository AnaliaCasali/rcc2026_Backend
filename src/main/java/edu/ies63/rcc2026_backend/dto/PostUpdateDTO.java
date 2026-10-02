package edu.ies63.rcc2026_backend.dto;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record PostUpdateDTO(
    @Size(max = 50, message = "El título no puede superar los 50 caracteres")
    String titulo,

    @Size(max = 280, message = "La descripción no puede superar los 280 caracteres")
    String descripcion,

    @URL(message = "El enlace del video debe ser una URL válida")
    String linkVideo,

    // permitimos la modificacion de imagenes
    List<MultipartFile> imagenes,
    List<Long> imagenesAEliminar
) {
}
