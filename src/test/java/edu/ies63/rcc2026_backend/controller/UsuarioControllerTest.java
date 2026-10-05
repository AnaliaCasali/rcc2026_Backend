package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.PerfilResponseDTO;
import edu.ies63.rcc2026_backend.dto.PerfilUpdateDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioResponseDTO;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.security.dto.CambiarPasswordRequest;
import edu.ies63.rcc2026_backend.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

  @Mock
  private UsuarioService usuarioService;

  @Mock
  private Principal principal;

  @InjectMocks
  private UsuarioController usuarioController;

  private PerfilResponseDTO perfilResponseMock;

  @BeforeEach
  void setUp() {
    perfilResponseMock = new PerfilResponseDTO(
        1L, "test@test.com", Role.USUARIO, "Usuario Test", "Bio",
        null, 1L, "Resistencia, CHACO", "123456", List.of(), List.of()
    );
  }

  @Test
  void listaUsuarios_DebeRetornarPaginaDeUsuarios() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 4);
    Page<UsuarioResponseDTO> paginaMock = new PageImpl<>(List.of(
        new UsuarioResponseDTO(1L, "test@test.com", Role.USUARIO, "Usuario Test", "Bio")
    ));
    when(usuarioService.listarUsuarios(pageable)).thenReturn(paginaMock);

    // Act
    ResponseEntity<Page<UsuarioResponseDTO>> response = usuarioController.listaUsuarios(pageable);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().getTotalElements());
    verify(usuarioService, times(1)).listarUsuarios(pageable);
  }

  @Test
  void actualizarPerfil_DebeRetornarPerfilActualizado() {
    // Arrange
    PerfilUpdateDTO request = new PerfilUpdateDTO(
        "Nuevo Nombre", "Nueva Bio", null, "123456",
        "insta", "face", "x", null, null
    );
    when(principal.getName()).thenReturn("test@test.com");
    when(usuarioService.actualizarPerfil(eq("test@test.com"), any(PerfilUpdateDTO.class)))
        .thenReturn(perfilResponseMock);

    // Act
    ResponseEntity<PerfilResponseDTO> response = usuarioController.actualizarPerfil(request, principal);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("test@test.com", response.getBody().username());
    verify(usuarioService, times(1)).actualizarPerfil("test@test.com", request);
  }

  @Test
  void obtenerMiPerfil_DebeRetornarPerfilDelUsuarioAutenticado() {
    // Arrange
    when(principal.getName()).thenReturn("test@test.com");
    when(usuarioService.obtenerMiPerfil("test@test.com")).thenReturn(perfilResponseMock);

    // Act
    ResponseEntity<PerfilResponseDTO> response = usuarioController.obtenerMiPerfil(principal);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Usuario Test", response.getBody().nombre());
    verify(usuarioService, times(1)).obtenerMiPerfil("test@test.com");
  }

  @Test
  void verPerfilPublico_DebeRetornarPerfilPorId() {
    // Arrange
    Long userId = 1L;
    when(usuarioService.verPerfilPublico(userId)).thenReturn(perfilResponseMock);

    // Act
    ResponseEntity<PerfilResponseDTO> response = usuarioController.verPerfilPublico(userId);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(userId, response.getBody().id());
    verify(usuarioService, times(1)).verPerfilPublico(userId);
  }

  @Test
  void cambiarPassword_DebeRetornarNoContentYDelegarEnService() {
    // Arrange
    CambiarPasswordRequest request = new CambiarPasswordRequest(
            "PasswordActual123!",
            "PasswordNueva123!",
            "PasswordNueva123!"
    );
    doNothing().when(usuarioService).cambiarPassword(request, principal);

    // Act
    ResponseEntity<Void> response = usuarioController.cambiarPassword(request, principal);

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(usuarioService, times(1)).cambiarPassword(request, principal);
  }
}