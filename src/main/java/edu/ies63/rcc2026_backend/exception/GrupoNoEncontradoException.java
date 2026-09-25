package edu.ies63.rcc2026_backend.exception;

public class GrupoNoEncontradoException extends RuntimeException {
  public GrupoNoEncontradoException(Long id) {
    super("Grupo con id " + id + " no encontrado");
  }
}