package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.EstadoEvento;
import java.time.LocalDate;
import java.time.LocalTime;

public record EventoCalendarioDTO(
        Long id,
        String nombre,
        LocalDate fecha,
        LocalTime hora,
        String localidad,
        EstadoEvento estado,
        Long organizadorId,
        String organizadorNombre
) {}