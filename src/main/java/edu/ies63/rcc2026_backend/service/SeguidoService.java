package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.SeguidoResponseDTO;
import edu.ies63.rcc2026_backend.model.Seguido;

import java.util.List;

public interface SeguidoService {

  boolean toggleSeguimiento(Long usuarioId, Long grupoId);

  boolean estaSiguiendo(Long usuarioId, Long grupoId);

  long contarSeguidores(Long grupoId);
  // grupos a los que sigue el usuario
  List<SeguidoResponseDTO> listarSeguidos(Long usuarioId);

  // usuarios que siguen al grupo
  List<SeguidoResponseDTO> listarSeguidores(Long grupoId);
}


