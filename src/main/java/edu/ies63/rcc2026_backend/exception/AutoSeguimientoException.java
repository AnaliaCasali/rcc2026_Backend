package edu.ies63.rcc2026_backend.exception;

public class AutoSeguimientoException extends RuntimeException {
  public AutoSeguimientoException() {
    super("Un usuario no puede seguirse a sí mismo");
  }
}
