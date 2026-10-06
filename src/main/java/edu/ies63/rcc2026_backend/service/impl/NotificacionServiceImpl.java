package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.NotificacionResponseDTO;
import edu.ies63.rcc2026_backend.model.Notificacion;
import edu.ies63.rcc2026_backend.repository.NotificacionRepository;
import edu.ies63.rcc2026_backend.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

  private final NotificacionRepository notificacionRepository;

  @Override
  @Transactional(readOnly = true)
  public List<NotificacionResponseDTO> listarMias(String username) {
    return notificacionRepository.findByPerfil_UsernameOrderByFechaCreacionDesc(username).stream()
        .map(n -> new NotificacionResponseDTO(
            n.getId(),
            n.getMensaje(),
            Boolean.TRUE.equals(n.getLeida()),
            n.getFechaCreacion(),
            n.getEventoId()))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public long contarNoLeidas(String username) {
    return notificacionRepository.countByPerfil_UsernameAndLeidaFalse(username);
  }

  @Override
  @Transactional
  public void marcarLeida(Long id, String username) {
    Notificacion n = notificacionRepository.findByIdAndPerfil_Username(id, username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no encontrada"));
    n.setLeida(true);
    notificacionRepository.save(n);
  }
}