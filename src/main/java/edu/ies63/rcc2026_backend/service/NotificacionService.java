package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.NotificacionResponseDTO;

import java.util.List;

public interface NotificacionService {
  List<NotificacionResponseDTO> listarMias(String username);
  long contarNoLeidas(String username);
  void marcarLeida(Long id, String username);
}