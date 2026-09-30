package edu.ies63.rcc2026_backend.exception;

public class NoEsGrupoException extends RuntimeException {
  public NoEsGrupoException(Long id) {
    super("El usuario con id " + id + " no es una agrupación ni un organizador");
  }
}
