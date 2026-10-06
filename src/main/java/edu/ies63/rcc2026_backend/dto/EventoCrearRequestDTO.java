package edu.ies63.rcc2026_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EventoCrearRequestDTO(
        @NotBlank(message = "El evento debe tener un nombre")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El evento debe tener un detalle")
        @Size(max = 500, message = "El detalle no puede superar los 500 caracteres")
        String detalle,

        @NotNull(message = "Debes indicar la fecha del evento")
        LocalDate fecha,

        @NotNull(message = "Debes indicar la hora del evento")
        LocalTime hora,

        @NotNull(message = "Debes indicar la localidad del evento")
        Long localidadId,

        @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
        String direccion,

        @NotNull(message = "Debes invitar al menos un artista o grupo")
        @Size(min = 1, max = 20, message = "Debes invitar al menos un artista o grupo")
        List<Long> artistaIds
) {}