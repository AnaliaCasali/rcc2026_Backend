package edu.ies63.rcc2026_backend.dto;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record PerfilUpdateDTO(
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,

    @Size(max = 500, message = "La biografía no puede superar los 500 caracteres")
    String biografia,

    @Size(max = 100, message = "El nombre del director no puede superar los 100 caracteres")
    String director,

    String whatsapp,
    String instagram,
    String facebook,
    String x,
    MultipartFile fotoPerfil,
    MultipartFile fotoBanner

    // Long localidadId
) {}