package edu.ies63.rcc2026_backend.exception;

import lombok.Getter;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class EventoConflictException extends RuntimeException {

    private final String codigo;
    private final Map<String, Object> details = new LinkedHashMap<>();

    public EventoConflictException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
        this.details.put("codigo", codigo);
    }

    public EventoConflictException(String codigo, String message, LocalDate editableCriticosHasta) {
        super(message);
        this.codigo = codigo;
        this.details.put("codigo", codigo);
        this.details.put("editableCriticosHasta", editableCriticosHasta.toString());
    }
}