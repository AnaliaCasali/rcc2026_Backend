package edu.ies63.rcc2026_backend.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EventoCreateDTO(
        @NotBlank(message = "El nombre del evento es obligatorio")
        String nombre,

        @NotBlank(message = "El detalle del evento es obligatorio")
        String descripcion,

        @NotNull(message = "La fecha del evento es obligatoria")
        @FutureOrPresent(message = "La fecha no puede ser en el pasado")
        LocalDate fechaEvento,

        @NotNull(message = "La hora del evento es obligatoria")
        LocalTime horaEvento,

        @NotBlank(message = "La ubicación es obligatoria")
        String ubicacion,

        @NotNull(message = "La localidad es obligatoria")
        Long localidadId,

        @NotEmpty(message = "Debe invitar al menos a un artista o grupo")
        List<Long> artistasIds
) {}