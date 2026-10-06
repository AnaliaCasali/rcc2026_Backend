package edu.ies63.rcc2026_backend.dto;

import java.time.LocalDate;

public record PermisosEventoDTO(
        boolean esOrganizador,
        boolean puedeEditarBasicos,
        boolean puedeEditarCriticos,
        LocalDate editableCriticosHasta
) {}