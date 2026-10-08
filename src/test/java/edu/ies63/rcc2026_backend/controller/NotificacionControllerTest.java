package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.NotificacionResponseDTO;
import edu.ies63.rcc2026_backend.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

  @Mock
  private NotificacionService notificacionService;

  @Mock
  private Principal principal;

  @InjectMocks
  private NotificacionController notificacionController;

  private final String username = "test@test.com";

  @BeforeEach
  void setUp() {
    lenient().when(principal.getName()).thenReturn(username);
  }

  @Test
  @DisplayName("listarMias devuelve 200 OK y la lista de notificaciones")
  void listarMias_DebeRetornarListaYStatus200() {
    List<NotificacionResponseDTO> lista = List.of(new NotificacionResponseDTO(1L, "Msg", false, LocalDateTime.now(), 1L));
    when(notificacionService.listarMias(username)).thenReturn(lista);

    ResponseEntity<List<NotificacionResponseDTO>> response = notificacionController.listarMias(principal);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
    verify(notificacionService).listarMias(username);
  }

  @Test
  @DisplayName("contarNoLeidas devuelve 200 OK y un mapa con la cantidad")
  void contarNoLeidas_DebeRetornarCantidad() {
    when(notificacionService.contarNoLeidas(username)).thenReturn(5L);

    ResponseEntity<Map<String, Long>> response = notificacionController.contarNoLeidas(principal);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsEntry("count", 5L);
    verify(notificacionService).contarNoLeidas(username);
  }

  @Test
  @DisplayName("marcarLeida devuelve 204 NO_CONTENT")
  void marcarLeida_DebeRetornarOk() {
    doNothing().when(notificacionService).marcarLeida(1L, username);

    ResponseEntity<Void> response = notificacionController.marcarLeida(1L, principal);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(notificacionService).marcarLeida(1L, username);
  }
}