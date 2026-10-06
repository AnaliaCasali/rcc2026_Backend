package edu.ies63.rcc2026_backend.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EventoEditarRequestDTO(
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @Size(max = 500, message = "El detalle no puede superar los 500 caracteres")
        String detalle,

        LocalDate fecha,
        LocalTime hora,
        Long localidadId,

        @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
        String direccion,

        List<Long> agregarArtistaIds,
        List<Long> quitarArtistaIds
) {
    public boolean tieneCambios() {
        return nombre != null || detalle != null || fecha != null || hora != null
                || localidadId != null || direccion != null
                || (agregarArtistaIds != null && !agregarArtistaIds.isEmpty())
                || (quitarArtistaIds != null && !quitarArtistaIds.isEmpty());
    }
}