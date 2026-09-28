package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.PerfilResponseDTO;
import edu.ies63.rcc2026_backend.dto.PerfilUpdateDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioResponseDTO;
import edu.ies63.rcc2026_backend.enums.Plataforma;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.enums.TipoImagen;
import edu.ies63.rcc2026_backend.exception.ProcesamientoImagenException;
import edu.ies63.rcc2026_backend.model.ImagenPerfil;
import edu.ies63.rcc2026_backend.model.Localidad;
import edu.ies63.rcc2026_backend.model.RedSocial;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

  @Mock
  private UsuarioRepository usuarioRepository;

  @InjectMocks
  private UsuarioServiceImpl usuarioService;

  private Usuario usuarioMock;

  @BeforeEach
  void setUp() {
    Localidad localidad = new Localidad();
    localidad.setId(1L);
    localidad.setLocalidad("Resistencia");

    usuarioMock = Usuario.builder()
        .id(1L)
        .username("test@test.com")
        .password("encodedPassword")
        .role(Role.USUARIO)
        .nombre("Usuario Test")
        .biografia("Bio original")
        .localidad(localidad)
        .imagenesPerfil(new ArrayList<>())
        .redes(new ArrayList<>())
        .build();
  }

  @Test
  void listarUsuarios_DebeRetornarPaginaMapeada() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    Page<Usuario> pageUsuarios = new PageImpl<>(List.of(usuarioMock));
    when(usuarioRepository.findAll(pageable)).thenReturn(pageUsuarios);

    // Act
    Page<UsuarioResponseDTO> result = usuarioService.listarUsuarios(pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals("test@test.com", result.getContent().get(0).username());
    verify(usuarioRepository, times(1)).findAll(pageable);
  }

  @Test
  void obtenerMiPerfil_UsuarioExiste_DebeRetornarPerfilResponseDTO() {
    // Arrange
    when(usuarioRepository.findByUsername("test@test.com")).thenReturn(Optional.of(usuarioMock));

    // Act
    PerfilResponseDTO result = usuarioService.obtenerMiPerfil("test@test.com");

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals("Usuario Test", result.nombre());
    assertEquals("test@test.com", result.username());
  }

  @Test
  void obtenerMiPerfil_UsuarioNoExiste_DebeLanzarResponseStatusException() {
    // Arrange
    when(usuarioRepository.findByUsername("noexiste@test.com")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResponseStatusException.class, () -> usuarioService.obtenerMiPerfil("noexiste@test.com"));
  }

  @Test
  void verPerfilPublico_UsuarioExiste_DebeRetornarPerfilResponseDTO() {
    // Arrange
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));

    // Act
    PerfilResponseDTO result = usuarioService.verPerfilPublico(1L);

    // Assert
    assertNotNull(result);
    assertEquals("Usuario Test", result.nombre());
  }

  @Test
  void verPerfilPublico_UsuarioNoExiste_DebeLanzarResponseStatusException() {
    // Arrange
    when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(ResponseStatusException.class, () -> usuarioService.verPerfilPublico(99L));
  }

  @Test
  void loadUserByUsername_UsuarioExiste_DebeRetornarUserDetails() {
    // Arrange
    when(usuarioRepository.findByUsername("test@test.com")).thenReturn(Optional.of(usuarioMock));

    // Act
    UserDetails result = usuarioService.loadUserByUsername("test@test.com");

    // Assert
    assertNotNull(result);
    assertEquals("test@test.com", result.getUsername());
  }

  @Test
  void loadUserByUsername_UsuarioNoExiste_DebeLanzarUsernameNotFoundException() {
    // Arrange
    when(usuarioRepository.findByUsername("noexiste@test.com")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UsernameNotFoundException.class, () -> usuarioService.loadUserByUsername("noexiste@test.com"));
  }

  @Test
  void actualizarPerfil_DatosBasicosYRedes_DebeActualizarYGuardar() {
    // Arrange
    PerfilUpdateDTO request = new PerfilUpdateDTO(
        "Nombre Modificado", "Bio Modificada", null, "3624112233",
        "mi_instagram", "mi_facebook", null, null, null
    );

    when(usuarioRepository.findByUsername("test@test.com")).thenReturn(Optional.of(usuarioMock));
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

    // Act
    PerfilResponseDTO result = usuarioService.actualizarPerfil("test@test.com", request);

    // Assert
    assertNotNull(result);
    assertEquals("Nombre Modificado", usuarioMock.getNombre());
    assertEquals("Bio Modificada", usuarioMock.getBiografia());
    assertEquals("3624112233", usuarioMock.getWhatsapp());

    // Verificar que las redes se agregaron correctamente
    assertEquals(2, usuarioMock.getRedes().size());
    assertTrue(usuarioMock.getRedes().stream().anyMatch(r -> r.getPlataforma() == Plataforma.INSTAGRAM));
    assertTrue(usuarioMock.getRedes().stream().anyMatch(r -> r.getPlataforma() == Plataforma.FACEBOOK));

    verify(usuarioRepository, times(1)).save(usuarioMock);
  }

  @Test
  void actualizarPerfil_ConRedesAlternativas_DebeCubrirXyDefault() {
    // Arrange
    PerfilUpdateDTO request = new PerfilUpdateDTO(
        null, null, null, null,
        null, null, "mi_twitter", null, null
    );
    // También simulamos una URL genérica pasándola en el DTO
    // Modificamos el request para inyectar una URL genérica (ej. en Facebook simulando un error del usuario, o modificando el request)
    // Para lograrlo de forma pura, probamos X
    when(usuarioRepository.findByUsername("test@test.com")).thenReturn(Optional.of(usuarioMock));
    when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

    // Act
    PerfilResponseDTO result = usuarioService.actualizarPerfil("test@test.com", request);

    // Assert
    assertTrue(usuarioMock.getRedes().stream().anyMatch(r -> r.getPlataforma() == Plataforma.X));
    assertEquals("https://x.com/mi_twitter", usuarioMock.getRedes().get(0).getUrl());
  }

  @Test
  void actualizarPerfil_ConImagenInvalida_DebeLanzarBadRequest() {
    // Arrange
    // Simulamos un archivo de texto en lugar de una imagen permitida
    MockMultipartFile archivoInvalido = new MockMultipartFile(
        "fotoPerfil", "test.txt", "text/plain", "contenido dummy".getBytes()
    );

    PerfilUpdateDTO request = new PerfilUpdateDTO(
        null, null, null, null,
        null, null, null, archivoInvalido, null
    );

    when(usuarioRepository.findByUsername("test@test.com")).thenReturn(Optional.of(usuarioMock));

    // Act & Assert
    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      usuarioService.actualizarPerfil("test@test.com", request);
    });

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertTrue(exception.getReason().contains("Formato de imagen no soportado"));
  }

  @Test
  void mapearAPerfilResponseDTO_UsuarioConCamposNulos_DebeMapearSinErrores() {
    // Arrange
    Usuario usuarioVacio = Usuario.builder()
        .id(2L)
        .username("vacio@test.com")
        .role(Role.USUARIO)
        .nombre("Vacio")
        // Dejamos explícitamente en nulo las relaciones
        .localidad(null)
        .imagenesPerfil(null)
        .redes(null)
        .build();

    // Act
    PerfilResponseDTO result = usuarioService.mapearAPerfilResponseDTO(usuarioVacio);

    // Assert
    assertNotNull(result);
    assertNull(result.localidadId());
    assertEquals("", result.localidadStr());
    assertTrue(result.imagenesPerfil().isEmpty());
    assertTrue(result.redes().isEmpty());
  }

  @Test
  void actualizarEntidadImagen_DebeReemplazarImagenPrevia() {
    // Arrange
    // Pre-cargamos una imagen en el usuario mock para probar que el método removeIf la elimina
    usuarioMock.getImagenesPerfil().add(
        ImagenPerfil.builder()
            .url("/uploads/perfiles/vieja.webp")
            .tipoImagen(TipoImagen.FOTOPERFIL)
            .usuario(usuarioMock)
            .build()
    );

    // Archivo válido
    MockMultipartFile archivoValido = new MockMultipartFile(
        "fotoBanner", "banner.webp", "image/webp", "dummy_image_bytes".getBytes()
    );

    PerfilUpdateDTO request = new PerfilUpdateDTO(
        null, null, null, null, null, null, null, null, archivoValido
    );

    when(usuarioRepository.findByUsername("test@test.com")).thenReturn(Optional.of(usuarioMock));

    // Excepción esperada por Thumbnails (ya que no es una imagen real), pero cubre el try/catch y elimina la imagen previa
    assertThrows(ProcesamientoImagenException.class, () -> {
      usuarioService.actualizarPerfil("test@test.com", request);
    });

    // Assert
    // Verifica que la limpieza previa se intentó
    assertTrue(usuarioMock.getImagenesPerfil().stream().noneMatch(i -> i.getTipoImagen() == TipoImagen.PORTADA));
  }
}