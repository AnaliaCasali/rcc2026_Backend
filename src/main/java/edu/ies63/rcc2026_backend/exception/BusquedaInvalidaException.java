package edu.ies63.rcc2026_backend.exception;

public class BusquedaInvalidaException extends RuntimeException {

    public BusquedaInvalidaException(String mensaje) {
        super(mensaje);
    }
}