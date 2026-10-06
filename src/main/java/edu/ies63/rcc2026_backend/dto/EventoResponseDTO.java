package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.EstadoEvento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record EventoResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        LocalDate fechaEvento,
        LocalTime horaEvento,
        String ubicacion,
        String localidadNombre,
        String organizadorNombre,
        Long organizadorId,
        EstadoEvento estado,
        LocalDateTime fechaPublic,
        List<ArtistaInvitadoDTO> artistas
) {}