package edu.ies63.rcc2026_backend.dto;

import java.util.List;

public record EventoEdicionResponseDTO(
        EventoDetalleResponseDTO evento,
        NotificacionEdicionResumen notificacion
) {
    public record NotificacionEdicionResumen(
            boolean enviada,
            int destinatarios,
            List<String> camposModificados
    ) {}
}