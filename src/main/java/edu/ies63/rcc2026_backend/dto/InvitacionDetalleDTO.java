package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.EstadoInvitacion;

import java.time.LocalDate;
import java.time.LocalTime;

public record InvitacionDetalleDTO(
        Long idInvitacion,
        Long idEvento,
        String nombreEvento,
        String detalle,
        LocalDate fechaEvento,
        LocalTime horaEvento,
        String direccion,
        String localidadNombre,
        String organizadorNombre,
        EstadoInvitacion estadoInvitacion
) {}