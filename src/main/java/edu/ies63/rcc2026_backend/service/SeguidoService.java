package edu.ies63.rcc2026_backend.service;

public interface SeguidoService {

  boolean toggleSeguimiento(Long usuarioId, Long grupoId);

  boolean estaSiguiendo(Long usuarioId, Long grupoId);

  long contarSeguidores(Long grupoId);
}