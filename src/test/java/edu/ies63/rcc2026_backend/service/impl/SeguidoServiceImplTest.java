package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.SeguidoResponseDTO;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.enums.TipoImagen;
import edu.ies63.rcc2026_backend.exception.AutoSeguimientoException;
import edu.ies63.rcc2026_backend.exception.GrupoNoEncontradoException;
import edu.ies63.rcc2026_backend.exception.NoEsGrupoException;
import edu.ies63.rcc2026_backend.exception.UsuarioNoEncontradoException;
import edu.ies63.rcc2026_backend.model.ImagenPerfil;
import edu.ies63.rcc2026_backend.model.Seguido;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.SeguidoRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeguidoServiceImplTest {

  @Mock
  private SeguidoRepository seguidoRepository;

  @Mock
  private UsuarioRepository usuarioRepository;

  @InjectMocks
  private SeguidoServiceImpl seguidoService;

  private Usuario usuarioMock;
  private Usuario grupoMock;
  private Seguido seguidoMock;

  @BeforeEach
  void setUp() {
    usuarioMock = Usuario.builder()
        .id(1L)
        .username("usuario@test.com")
        .role(Role.USUARIO)
        .nombre("Usuario Test")
        .build();

    grupoMock = Usuario.builder()
        .id(2L)
        .username("grupo@test.com")
        .role(Role.GRUPO)
        .nombre("Grupo Test")
        .build();

    seguidoMock = Seguido.builder()
        .id(10L)
        .usuario(usuarioMock)
        .grupo(grupoMock)
        .build();
  }

  // ---------- toggleSeguimiento ----------

  @Test
  void toggleSeguimiento_UsuarioIgualAGrupo_DebeLanzarAutoSeguimientoException() {
    // Act & Assert
    assertThrows(
        AutoSeguimientoException.class,
        () -> seguidoService.toggleSeguimiento(1L, 1L)
    );

    verifyNoInteractions(usuarioRepository, seguidoRepository);
  }

  @Test
  void toggleSeguimiento_UsuarioNoExiste_DebeLanzarUsuarioNoEncontradoException() {
    // Arrange
    when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        UsuarioNoEncontradoException.class,
        () -> seguidoService.toggleSeguimiento(1L, 2L)
    );

    verify(usuarioRepository, times(1)).findById(1L);
    verifyNoInteractions(seguidoRepository);
  }

  @Test
  void toggleSeguimiento_GrupoNoExiste_DebeLanzarGrupoNoEncontradoException() {
    // Arrange
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        GrupoNoEncontradoException.class,
        () -> seguidoService.toggleSeguimiento(1L, 2L)
    );

    verifyNoInteractions(seguidoRepository);
  }

  @Test
  void toggleSeguimiento_UsuarioIndicadoNoEsGrupo_DebeLanzarNoEsGrupoException() {
    // Arrange: "grupo" en realidad es un usuario común (role USUARIO)
    Usuario noEsGrupo = Usuario.builder()
        .id(2L)
        .role(Role.USUARIO)
        .nombre("No es grupo")
        .build();

    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(noEsGrupo));

    // Act & Assert
    assertThrows(
        NoEsGrupoException.class,
        () -> seguidoService.toggleSeguimiento(1L, 2L)
    );

    verifyNoInteractions(seguidoRepository);
  }

  @Test
  void toggleSeguimiento_NoSeguiaAntes_DebeGuardarNuevoSeguidoYRetornarTrue() {
    // Arrange
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(grupoMock));
    when(seguidoRepository.findByUsuarioIdAndGrupoId(1L, 2L)).thenReturn(Optional.empty());

    // Act
    boolean result = seguidoService.toggleSeguimiento(1L, 2L);

    // Assert
    assertTrue(result);

    ArgumentCaptor<Seguido> captor = ArgumentCaptor.forClass(Seguido.class);
    verify(seguidoRepository, times(1)).save(captor.capture());

    Seguido guardado = captor.getValue();
    assertEquals(usuarioMock, guardado.getUsuario());
    assertEquals(grupoMock, guardado.getGrupo());
    verify(seguidoRepository, never()).delete(any());
  }

  @Test
  void toggleSeguimiento_YaSeguia_DebeEliminarSeguidoYRetornarFalse() {
    // Arrange
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioMock));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(grupoMock));
    when(seguidoRepository.findByUsuarioIdAndGrupoId(1L, 2L)).thenReturn(Optional.of(seguidoMock));

    // Act
    boolean result = seguidoService.toggleSeguimiento(1L, 2L);

    // Assert
    assertFalse(result);
    verify(seguidoRepository, times(1)).delete(seguidoMock);
    verify(seguidoRepository, never()).save(any());
  }

  // ---------- estaSiguiendo ----------

  @Test
  void estaSiguiendo_CuandoElRepositoryDiceQueSi_DebeRetornarTrue() {
    // Arrange
    when(seguidoRepository.existsByUsuarioIdAndGrupoId(1L, 2L)).thenReturn(true);

    // Act
    boolean result = seguidoService.estaSiguiendo(1L, 2L);

    // Assert
    assertTrue(result);
  }

  @Test
  void estaSiguiendo_CuandoElRepositoryDiceQueNo_DebeRetornarFalse() {
    // Arrange
    when(seguidoRepository.existsByUsuarioIdAndGrupoId(1L, 2L)).thenReturn(false);

    // Act
    boolean result = seguidoService.estaSiguiendo(1L, 2L);

    // Assert
    assertFalse(result);
  }

  // ---------- contarSeguidores / contarSeguidos ----------

  @Test
  void contarSeguidores_DebeRetornarCantidadDelRepository() {
    // Arrange
    when(seguidoRepository.countByGrupoId(2L)).thenReturn(5L);

    // Act
    long result = seguidoService.contarSeguidores(2L);

    // Assert
    assertEquals(5L, result);
  }

  @Test
  void contarSeguidos_DebeRetornarCantidadDelRepository() {
    // Arrange
    when(seguidoRepository.countByUsuarioId(1L)).thenReturn(3L);

    // Act
    long result = seguidoService.contarSeguidos(1L);

    // Assert
    assertEquals(3L, result);
  }

  // ---------- listarSeguidos ----------

  @Test
  void listarSeguidos_DebeMapearElGrupoSeguidoConFlagSeguidoEnTrue() {
    // Arrange
    when(seguidoRepository.findByUsuarioId(1L)).thenReturn(List.of(seguidoMock));

    // Act
    List<SeguidoResponseDTO> result = seguidoService.listarSeguidos(1L);

    // Assert
    assertEquals(1, result.size());
    SeguidoResponseDTO dto = result.get(0);
    assertEquals(seguidoMock.getId(), dto.id());
    assertEquals(grupoMock.getId(), dto.usuarioId());
    assertEquals(grupoMock.getNombre(), dto.nombre());
    assertEquals(Role.GRUPO, dto.tipoUsuario());
    assertTrue(dto.seguido()); // siempre true en listarSeguidos

    // No debería tener que consultar si lo sigue: ya se sabe que sí
    verify(seguidoRepository, never()).existsByUsuarioIdAndGrupoId(any(), any());
  }

  @Test
  void listarSeguidos_GrupoSinImagenesPerfil_DebeRetornarFotoPerfilUrlNull() {
    // Arrange: grupoMock no tiene imagenesPerfil seteado (null por defecto)
    when(seguidoRepository.findByUsuarioId(1L)).thenReturn(List.of(seguidoMock));

    // Act
    List<SeguidoResponseDTO> result = seguidoService.listarSeguidos(1L);

    // Assert
    assertNull(result.get(0).fotoPerfilUrl());
  }

  @Test
  void listarSeguidos_GrupoConImagenPerfil_DebeRetornarLaUrl() {
    // Arrange
    ImagenPerfil imagen = ImagenPerfil.builder()
        .id(100L)
        .url("/uploads/perfiles/grupo.webp")
        .tipoImagen(TipoImagen.FOTOPERFIL)
        .build();

    Usuario grupoConFoto = Usuario.builder()
        .id(2L)
        .role(Role.GRUPO)
        .nombre("Grupo Test")
        .imagenesPerfil(List.of(imagen))
        .build();

    Seguido seguidoConFoto = Seguido.builder()
        .id(10L)
        .usuario(usuarioMock)
        .grupo(grupoConFoto)
        .build();

    when(seguidoRepository.findByUsuarioId(1L)).thenReturn(List.of(seguidoConFoto));

    // Act
    List<SeguidoResponseDTO> result = seguidoService.listarSeguidos(1L);

    // Assert
    assertEquals("/uploads/perfiles/grupo.webp", result.get(0).fotoPerfilUrl());
  }

  @Test
  void listarSeguidos_SinSeguidos_DebeRetornarListaVacia() {
    // Arrange
    when(seguidoRepository.findByUsuarioId(1L)).thenReturn(List.of());

    // Act
    List<SeguidoResponseDTO> result = seguidoService.listarSeguidos(1L);

    // Assert
    assertTrue(result.isEmpty());
  }

  // ---------- listarSeguidores ----------

  @Test
  void listarSeguidores_CuandoElLogueadoTambienLoSigue_DebeRetornarFlagSeguidoEnTrue() {
    // Arrange: grupoMock (id 2) lista sus seguidores, y entre ellos esta usuarioMock (id 1)
    when(seguidoRepository.findByGrupoId(2L)).thenReturn(List.of(seguidoMock));
    when(seguidoRepository.existsByUsuarioIdAndGrupoId(2L, 1L)).thenReturn(true);

    // Act
    List<SeguidoResponseDTO> result = seguidoService.listarSeguidores(2L);

    // Assert
    assertEquals(1, result.size());
    SeguidoResponseDTO dto = result.get(0);
    assertEquals(usuarioMock.getId(), dto.usuarioId());
    assertEquals(usuarioMock.getNombre(), dto.nombre());
    assertEquals(Role.USUARIO, dto.tipoUsuario());
    assertTrue(dto.seguido());

    verify(seguidoRepository, times(1)).existsByUsuarioIdAndGrupoId(2L, 1L);
  }

  @Test
  void listarSeguidores_CuandoElLogueadoNoLoSigue_DebeRetornarFlagSeguidoEnFalse() {
    // Arrange
    when(seguidoRepository.findByGrupoId(2L)).thenReturn(List.of(seguidoMock));
    when(seguidoRepository.existsByUsuarioIdAndGrupoId(2L, 1L)).thenReturn(false);

    // Act
    List<SeguidoResponseDTO> result = seguidoService.listarSeguidores(2L);

    // Assert
    assertFalse(result.get(0).seguido());
  }

  @Test
  void listarSeguidores_SinSeguidores_DebeRetornarListaVacia() {
    // Arrange
    when(seguidoRepository.findByGrupoId(2L)).thenReturn(List.of());

    // Act
    List<SeguidoResponseDTO> result = seguidoService.listarSeguidores(2L);

    // Assert
    assertTrue(result.isEmpty());
    verifyNoInteractions(usuarioRepository);
  }
}