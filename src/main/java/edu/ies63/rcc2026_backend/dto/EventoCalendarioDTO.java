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
    String organizadorNombre,
    Boolean activo // NUEVO: false = desactivado por quien lo creó (no se muestra a los demás)
     ) { /** NUEVO: firma anterior (sin activo); asume activo = true. Mantiene compatibles los tests. */
public EventoCalendarioDTO(Long id, String nombre, LocalDate fecha, LocalTime hora, String localidad, EstadoEvento estado, Long organizadorId, String organizadorNombre)
{ this(id, nombre, fecha, hora, localidad, estado, organizadorId, organizadorNombre, true); } }