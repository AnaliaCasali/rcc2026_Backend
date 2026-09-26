package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.SeguidoResponseDTO;

import java.util.List;

public interface SeguidoService {
  boolean toggleSeguimiento(Long usuarioId, Long grupoId);
  boolean estaSiguiendo(Long usuarioId, Long grupoId);
  long contarSeguidores(Long grupoId);
  long contarSeguidos(Long usuarioId);
  List<SeguidoResponseDTO> listarSeguidos(Long usuarioId);
  List<SeguidoResponseDTO> listarSeguidores(Long grupoId);
}

