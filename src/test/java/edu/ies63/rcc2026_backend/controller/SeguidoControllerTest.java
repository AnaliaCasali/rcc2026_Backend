package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.SeguidoResponseDTO;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.service.SeguidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeguidoControllerTest {

  @Mock
  private SeguidoService seguidoService;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private SeguidoController seguidoController;

  private Usuario usuarioAutenticado;

  @BeforeEach
  void setUp() {
    usuarioAutenticado = new Usuario();
    usuarioAutenticado.setId(1L);
  }

  @Test
  @DisplayName("toggleSeguimiento: Retorna 200 OK con el estado y la cantidad de seguidores")
  void toggleSeguimiento_DebeRetornar200() {
    when(authentication.getPrincipal()).thenReturn(usuarioAutenticado);
    when(seguidoService.toggleSeguimiento(1L, 2L)).thenReturn(true);
    when(seguidoService.contarSeguidores(2L)).thenReturn(10L);

    ResponseEntity<Map<String, Object>> response = seguidoController.toggleSeguimiento(2L, authentication);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsEntry("siguiendo", true);
    assertThat(response.getBody()).containsEntry("seguidores", 10L);
    verify(seguidoService).toggleSeguimiento(1L, 2L);
    verify(seguidoService).contarSeguidores(2L);
  }

  @Test
  @DisplayName("estadoSeguimiento: Retorna 200 OK con el estado actual")
  void estadoSeguimiento_DebeRetornar200() {
    when(authentication.getPrincipal()).thenReturn(usuarioAutenticado);
    when(seguidoService.estaSiguiendo(1L, 2L)).thenReturn(false);
    when(seguidoService.contarSeguidores(2L)).thenReturn(5L);

    ResponseEntity<Map<String, Object>> response = seguidoController.estadoSeguimiento(2L, authentication);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsEntry("siguiendo", false);
    assertThat(response.getBody()).containsEntry("seguidores", 5L);
  }

  @Test
  @DisplayName("listarMisSeguidos: Retorna 200 OK con la lista de seguidos del usuario autenticado")
  void listarMisSeguidos_DebeRetornar200() {
    when(authentication.getPrincipal()).thenReturn(usuarioAutenticado);
    SeguidoResponseDTO dto = new SeguidoResponseDTO(100L, 2L, "Grupo Test", Role.GRUPO, "url", true);
    when(seguidoService.listarSeguidos(1L)).thenReturn(List.of(dto));

    ResponseEntity<List<SeguidoResponseDTO>> response = seguidoController.listarMisSeguidos(authentication);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsExactly(dto);
  }

  @Test
  @DisplayName("listarSeguidosDeOtroUsuario: Retorna 200 OK con la lista")
  void listarSeguidosDeOtroUsuario_DebeRetornar200() {
    SeguidoResponseDTO dto = new SeguidoResponseDTO(100L, 2L, "Grupo Test", Role.GRUPO, "url", true);
    when(seguidoService.listarSeguidos(5L)).thenReturn(List.of(dto));

    ResponseEntity<List<SeguidoResponseDTO>> response = seguidoController.listarSeguidosDeOtroUsuario(5L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsExactly(dto);
  }

  @Test
  @DisplayName("listarSeguidores: Retorna 200 OK con la lista de seguidores")
  void listarSeguidores_DebeRetornar200() {
    SeguidoResponseDTO dto = new SeguidoResponseDTO(100L, 1L, "Usuario Seguidor", Role.USUARIO, null, false);
    when(seguidoService.listarSeguidores(2L)).thenReturn(List.of(dto));

    ResponseEntity<List<SeguidoResponseDTO>> response = seguidoController.listarSeguidores(2L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsExactly(dto);
  }

  @Test
  @DisplayName("contarMisSeguidos: Retorna 200 OK con la cantidad")
  void contarMisSeguidos_DebeRetornar200() {
    when(authentication.getPrincipal()).thenReturn(usuarioAutenticado);
    when(seguidoService.contarSeguidos(1L)).thenReturn(7L);

    ResponseEntity<Map<String, Object>> response = seguidoController.contarMisSeguidos(authentication);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsEntry("seguidos", 7L);
  }

  @Test
  @DisplayName("contarSeguidores: Retorna 200 OK con la cantidad")
  void contarSeguidores_DebeRetornar200() {
    when(seguidoService.contarSeguidores(2L)).thenReturn(15L);

    ResponseEntity<Map<String, Object>> response = seguidoController.contarSeguidores(2L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsEntry("seguidores", 15L);
  }
}