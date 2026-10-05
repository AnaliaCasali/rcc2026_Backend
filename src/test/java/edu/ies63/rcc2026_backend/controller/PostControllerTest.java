package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.PostCreateDTO;
import edu.ies63.rcc2026_backend.dto.PostResponseDTO;
import edu.ies63.rcc2026_backend.dto.PostUpdateDTO;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.enums.TipoPost;
import edu.ies63.rcc2026_backend.service.PostService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

  @Mock
  private PostService postService;

  @Mock
  private Principal principal;

  @InjectMocks
  private PostController postController;

  private PostResponseDTO postResponseMock;

  @BeforeEach
  void setUp() {
    postResponseMock = new PostResponseDTO(
        1L, "Título de Prueba", "Descripción", TipoPost.POST, null, List.of(),
        LocalDateTime.now(), "usuarioTest", null, Role.USUARIO, 0, false
    );
  }

  @Test
  void listarPosts_ConUsuarioAutenticado_DebeRetornarPagina() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    Page<PostResponseDTO> paginaMock = new PageImpl<>(List.of(postResponseMock));

    when(principal.getName()).thenReturn("usuarioTest");
    when(postService.listarPosts(pageable, "usuarioTest")).thenReturn(paginaMock);

    // Act
    ResponseEntity<Page<PostResponseDTO>> response = postController.listarPosts(pageable, principal);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().getTotalElements());
    verify(postService, times(1)).listarPosts(pageable, "usuarioTest");
  }

  @Test
  void listarPosts_SinUsuarioAutenticado_DebeRetornarPagina() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    Page<PostResponseDTO> paginaMock = new PageImpl<>(List.of(postResponseMock));

    // Al pasar null como principal, el nombre debería procesarse como null
    when(postService.listarPosts(pageable, null)).thenReturn(paginaMock);

    // Act
    ResponseEntity<Page<PostResponseDTO>> response = postController.listarPosts(pageable, null);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(postService, times(1)).listarPosts(pageable, null);
  }

  @Test
  void feed_DebeRetornarPublicacionesDeSeguidos() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 20);
    Page<PostResponseDTO> paginaMock = new PageImpl<>(List.of(postResponseMock));

    when(principal.getName()).thenReturn("usuarioTest");
    when(postService.listarFeed(pageable, "usuarioTest")).thenReturn(paginaMock);

    // Act
    ResponseEntity<Page<PostResponseDTO>> response = postController.feed(pageable, principal);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(postService, times(1)).listarFeed(pageable, "usuarioTest");
  }

  @Test
  void listarPremios_DebeRetornarPaginaDePremios() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);
    Page<PostResponseDTO> paginaMock = new PageImpl<>(List.of(postResponseMock));

    when(principal.getName()).thenReturn("usuarioTest");
    when(postService.listarPremios(pageable, "usuarioTest")).thenReturn(paginaMock);

    // Act
    ResponseEntity<Page<PostResponseDTO>> response = postController.listarPremios(pageable, principal);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(postService, times(1)).listarPremios(pageable, "usuarioTest");
  }

  @Test
  void crearPost_DebeRetornarPostCreadoConEstatusCreated() {
    // Arrange
    PostCreateDTO request = new PostCreateDTO(
        "Nuevo Post", "Descripción", TipoPost.POST, null, List.of()
    );
    when(principal.getName()).thenReturn("usuarioTest");
    when(postService.crearPost(eq(request), eq("usuarioTest"))).thenReturn(postResponseMock);

    // Act
    ResponseEntity<PostResponseDTO> response = postController.crearPost(request, principal);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals("Título de Prueba", response.getBody().titulo());
    verify(postService, times(1)).crearPost(request, "usuarioTest");
  }

  @Test
  void editarPost_DebeRetornarPostEditado() {
    // Arrange
    Long idPost = 1L;
    PostUpdateDTO request = new PostUpdateDTO(
        "Título Editado", "Descripción Editada", null, null, null
    );
    when(principal.getName()).thenReturn("usuarioTest");
    when(postService.editarPost(eq(idPost), eq(request), eq("usuarioTest"))).thenReturn(postResponseMock);

    // Act
    ResponseEntity<PostResponseDTO> response = postController.editarPost(idPost, request, principal);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(postService, times(1)).editarPost(idPost, request, "usuarioTest");
  }

  @Test
  void eliminarPost_DebeRetornarEstatusNoContent() {
    // Arrange
    Long idPost = 1L;
    when(principal.getName()).thenReturn("usuarioTest");

    // Act
    ResponseEntity<Void> response = postController.eliminarPost(idPost, principal);

    // Assert
    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(postService, times(1)).eliminarPost(idPost, "usuarioTest");
  }
}