package edu.ies63.rcc2026_backend.dto;

import jakarta.validation.constraints.NotNull;

public record RespuestaInvitacionDTO(
        @NotNull(message = "Debe indicar si confirma o rechaza la invitación")
        Boolean aceptar
) {}