package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.NotificacionResponseDTO;
import edu.ies63.rcc2026_backend.model.Notificacion;
import edu.ies63.rcc2026_backend.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {

  @Mock
  private NotificacionRepository notificacionRepository;

  @InjectMocks
  private NotificacionServiceImpl notificacionService;

  private Notificacion notificacion;
  private final String username = "test@test.com";

  @BeforeEach
  void setUp() {
    notificacion = Notificacion.builder()
        .id(1L)
        .mensaje("Mensaje de prueba")
        .leida(false)
        .fechaCreacion(LocalDateTime.now())
        .eventoId(10L)
        .build();
  }

  @Test
  @DisplayName("Debe listar las notificaciones mapeadas a DTO")
  void listarMias_DebeRetornarListaDeDTOs() {
    when(notificacionRepository.findByPerfil_UsernameOrderByFechaCreacionDesc(username))
        .thenReturn(List.of(notificacion));

    List<NotificacionResponseDTO> resultado = notificacionService.listarMias(username);

    assertThat(resultado).hasSize(1);
    assertThat(resultado.get(0).mensaje()).isEqualTo("Mensaje de prueba");
    assertThat(resultado.get(0).leida()).isFalse();
    verify(notificacionRepository).findByPerfil_UsernameOrderByFechaCreacionDesc(username);
  }

  @Test
  @DisplayName("Debe contar las notificaciones no leídas")
  void contarNoLeidas_DebeRetornarCantidad() {
    when(notificacionRepository.countByPerfil_UsernameAndLeidaFalse(username))
        .thenReturn(5L);

    long count = notificacionService.contarNoLeidas(username);

    assertThat(count).isEqualTo(5L);
    verify(notificacionRepository).countByPerfil_UsernameAndLeidaFalse(username);
  }

  @Test
  @DisplayName("Debe marcar como leída y guardar si existe")
  void marcarLeida_DebeActualizarYGuardar_SiExiste() {
    when(notificacionRepository.findByIdAndPerfil_Username(1L, username))
        .thenReturn(Optional.of(notificacion));

    notificacionService.marcarLeida(1L, username);

    assertThat(notificacion.getLeida()).isTrue();
    verify(notificacionRepository).save(notificacion);
  }

  @Test
  @DisplayName("Lanza 404 si la notificación no existe para ese usuario")
  void marcarLeida_DebeLanzarExcepcion_SiNoExiste() {
    when(notificacionRepository.findByIdAndPerfil_Username(1L, username))
        .thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
        notificacionService.marcarLeida(1L, username)
    );

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    verify(notificacionRepository, never()).save(any());
  }
}