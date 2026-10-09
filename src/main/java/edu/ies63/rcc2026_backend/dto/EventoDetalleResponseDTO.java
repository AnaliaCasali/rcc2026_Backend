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
    PermisosEventoDTO permisos,
    Boolean activo  // NUEVO: false = desactivado por quien lo creó (no se muestra a los demás)
) {    /** NUEVO: firma anterior (sin activo); asume activo = true. Mantiene compatibles los tests. */
public EventoDetalleResponseDTO(
    Long id, String nombre, String detalle, LocalDate fecha, LocalTime hora,
      LocalidadSimpleDTO localidad, String direccion, EstadoEvento estado,
      OrganizadorSimpleDTO organizador, List<ArtistaDetalleDTO> artistas,
      PermisosEventoDTO permisos)
{        this(id, nombre, detalle, fecha, hora, localidad, direccion, estado, organizador, artistas, permisos, true);    } }