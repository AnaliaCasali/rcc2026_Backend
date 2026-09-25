package edu.ies63.rcc2026_backend.exception;

public class UsuarioNoEncontradoException extends RuntimeException {
  public UsuarioNoEncontradoException(Long id) {
    super("Usuario con id " + id + " no encontrado");
  }
}