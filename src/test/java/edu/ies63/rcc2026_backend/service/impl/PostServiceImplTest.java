package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.PostCreateDTO;
import edu.ies63.rcc2026_backend.dto.PostResponseDTO;
import edu.ies63.rcc2026_backend.dto.PostUpdateDTO;
import edu.ies63.rcc2026_backend.enums.TipoImagen;
import edu.ies63.rcc2026_backend.enums.TipoPost;
import edu.ies63.rcc2026_backend.exception.ProcesamientoImagenException;
import edu.ies63.rcc2026_backend.model.ImagenPerfil;
import edu.ies63.rcc2026_backend.model.ImagenPost;
import edu.ies63.rcc2026_backend.model.Post;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.PostLikeRepository;
import edu.ies63.rcc2026_backend.repository.PostRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

  @Mock
  private PostRepository postRepository;

  @Mock
  private UsuarioRepository usuarioRepository;

  @Mock
  private PostLikeRepository likeRepository;

  @InjectMocks
  private PostServiceImpl postService;

  private Usuario mockUsuario;
  private Post mockPost;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    mockUsuario = new Usuario();
    mockUsuario.setId(1L);
    mockUsuario.setUsername("testuser");
    mockUsuario.setNombre("Test User");

    mockPost = Post.builder()
        .id(1L)
        .titulo("Test Post")
        .descripcion("Descripción test")
        .tipoPost(TipoPost.POST)
        .usuario(mockUsuario)
        .fechaCreacion(LocalDateTime.now())
        .imagenes(new ArrayList<>())
        .build();

    pageable = PageRequest.of(0, 10);
  }

  // --- TESTS DE LECTURA ---

  @Test
  void listarPosts_PaginaVacia_DebeRetornarPaginaVacia() {
    when(postRepository.findByTipoPost(any(), any(Pageable.class))).thenReturn(Page.empty());
    Page<PostResponseDTO> result = postService.listarPosts(pageable, "testuser");
    assertTrue(result.isEmpty());
  }

  @Test
  void listarPosts_ConImagenesYLikes_DebeMapearCompleto() {
    mockPost.setImagenes(List.of(new ImagenPost(1L, "/img.webp", "alt", mockPost)));

    ImagenPerfil imgPerfil = new ImagenPerfil();
    imgPerfil.setTipoImagen(TipoImagen.FOTOPERFIL);
    imgPerfil.setUrl("/perfil.webp");
    mockUsuario.setImagenesPerfil(List.of(imgPerfil));

    when(postRepository.findByTipoPost(eq(TipoPost.POST), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockPost)));

    when(likeRepository.contarPorPosts(anyList())).thenReturn(Arrays.<Object[]>asList(new Object[]{1L, 10L}));
    when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUsuario));
    when(likeRepository.idsConLikeDe(eq(1L), anyList())).thenReturn(List.of(1L));

    Page<PostResponseDTO> result = postService.listarPosts(pageable, "testuser");

    assertFalse(result.isEmpty());
    PostResponseDTO dto = result.getContent().get(0);
    assertEquals(10L, dto.likesCount());
    assertTrue(dto.liked());
    assertEquals("/perfil.webp", dto.usuarioFotoPerfilUrl());
    assertEquals(1, dto.imagenes().size());
  }

  @Test
  void listarPremios_DebeRetornarPaginaVisitante() {
    when(postRepository.findByTipoPost(eq(TipoPost.PREMIO), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockPost)));

    Page<PostResponseDTO> result = postService.listarPremios(pageable, null);

    assertEquals(1, result.getTotalElements());
    assertFalse(result.getContent().get(0).liked());
  }

  @Test
  void listarPorUsuario_DebeRetornarPagina() {
    when(postRepository.findByUsuarioIdAndTipoPost(eq(1L), eq(TipoPost.POST), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockPost)));

    Page<PostResponseDTO> result = postService.listarPorUsuario(1L, pageable, "testuser");
    assertEquals(1, result.getTotalElements());
  }

  @Test
  void listarFeed_UsuarioEncontrado_DebeRetornarPagina() {
    when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUsuario));
    when(postRepository.findFeedDeSeguidos(eq(TipoPost.POST), eq(1L), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mockPost)));

    Page<PostResponseDTO> result = postService.listarFeed(pageable, "testuser");
    assertEquals(1, result.getTotalElements());
  }

  @Test
  void listarFeed_UsuarioNoEncontrado_DebeLanzarExcepcion() {
    when(usuarioRepository.findByUsername("unknown")).thenReturn(Optional.empty());
    assertThrows(ResponseStatusException.class, () -> postService.listarFeed(pageable, "unknown"));
  }

  // --- TESTS DE CREACIÓN ---

  @Test
  void crearPost_SinImagenes_DeberiaLanzarExcepcion() {
    when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUsuario));
    PostCreateDTO request = new PostCreateDTO("Titulo", "Desc", TipoPost.POST, null, List.of());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> postService.crearPost(request, "testuser"));
    assertTrue(ex.getMessage().contains("al menos una imagen"));
  }

  @Test
  void crearPost_TipoImagenInvalido_DebeLanzarExcepcion() {
    when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUsuario));
    MockMultipartFile file = new MockMultipartFile("imagenes", "test.pdf", "application/pdf", "data".getBytes());
    PostCreateDTO request = new PostCreateDTO("Titulo", "Desc", TipoPost.POST, null, List.of(file));

    assertThrows(ResponseStatusException.class, () -> postService.crearPost(request, "testuser"));
  }

  @Test
  void crearPost_ProcesamientoFallidoPorThumbnails_DebeLanzarExcepcion() {
    when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUsuario));
    MockMultipartFile file = new MockMultipartFile("imagenes", "test.jpg", "image/jpeg", "bytes-invalidos".getBytes());
    PostCreateDTO request = new PostCreateDTO("Titulo", "Desc", TipoPost.POST, null, List.of(file));

    assertThrows(ProcesamientoImagenException.class, () -> postService.crearPost(request, "testuser"));
  }

  // --- TESTS DE EDICIÓN ---

  @Test
  void editarPost_Exito_ModificandoCampos() {
    mockPost.setImagenes(new ArrayList<>(List.of(new ImagenPost(1L, "/img.webp", "alt", mockPost))));
    when(postRepository.findById(1L)).thenReturn(Optional.of(mockPost));
    when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUsuario));
    when(postRepository.save(any(Post.class))).thenReturn(mockPost);

    PostUpdateDTO request = new PostUpdateDTO("Nuevo titulo", "Nueva desc", "  ", null, null);

    PostResponseDTO result = postService.editarPost(1L, request, "testuser");

    assertEquals("Nuevo titulo", mockPost.getTitulo());
    assertEquals("Nueva desc", mockPost.getDescripcion());
    assertNull(mockPost.getLinkVideo());
    assertNotNull(result);
  }

  @Test
  void editarPost_AgregarYEliminarImagenesFisicas() {
    ImagenPost img1 = new ImagenPost(1L, "uploads/img1.webp", "alt", mockPost);
    mockPost.setImagenes(new ArrayList<>(List.of(img1)));

    when(postRepository.findById(1L)).thenReturn(Optional.of(mockPost));
    // when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUsuario));
    // Nota: Se removió el stubbing de postRepository.save porque la excepción ocurre antes de guardarse.

    MockMultipartFile file = new MockMultipartFile("imagenes", "test.jpg", "image/jpeg", "invalido".getBytes());
    PostUpdateDTO request = new PostUpdateDTO(null, null, null, List.of(file), List.of(1L));

    assertThrows(ProcesamientoImagenException.class, () -> postService.editarPost(1L, request, "testuser"));
    assertTrue(mockPost.getImagenes().isEmpty());
  }

  @Test
  void editarPost_EliminarUnicaImagenSinReponer_DebeFallarPorIntegridad() {
    mockPost.setImagenes(new ArrayList<>(List.of(new ImagenPost(1L, "/img.webp", "alt", mockPost))));
    when(postRepository.findById(1L)).thenReturn(Optional.of(mockPost));

    PostUpdateDTO request = new PostUpdateDTO(null, null, null, null, List.of(1L));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> postService.editarPost(1L, request, "testuser"));
    assertTrue(ex.getMessage().contains("al menos una imagen"));
  }

  @Test
  void editarPost_SinPermisos_DebeLanzarExcepcion() {
    when(postRepository.findById(1L)).thenReturn(Optional.of(mockPost));
    PostUpdateDTO request = new PostUpdateDTO("Titulo", null, null, null, null);
    assertThrows(ResponseStatusException.class, () -> postService.editarPost(1L, request, "otrouser"));
  }

  // --- TESTS DE ELIMINACIÓN ---

  @Test
  void eliminarPost_Exito() {
    mockPost.setImagenes(new ArrayList<>(List.of(new ImagenPost(1L, "/uploads/img.webp", "alt", mockPost))));
    when(postRepository.findById(1L)).thenReturn(Optional.of(mockPost));

    postService.eliminarPost(1L, "testuser");
    verify(postRepository, times(1)).delete(mockPost);
  }
}