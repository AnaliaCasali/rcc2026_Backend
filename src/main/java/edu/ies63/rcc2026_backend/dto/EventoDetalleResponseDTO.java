package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.enums.EstadoEvento;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EventoDetalleResponseDTO(
        Long id,
        String nombre,
        String detalle,
        LocalDate fecha,
        LocalTime hora,
        LocalidadSimpleDTO localidad,
        String direccion,
        EstadoEvento estado,
        OrganizadorSimpleDTO organizador,
        List<ArtistaDetalleDTO> artistas,
        PermisosEventoDTO permisos
) {}