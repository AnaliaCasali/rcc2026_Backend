package edu.ies63.rcc2026_backend.model;

import edu.ies63.rcc2026_backend.enums.TipoPost;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PostTest {

  @Test
  void testPostBuilderYGetters() {
    // Arrange
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    usuario.setUsername("autorTest");

    LocalDateTime fecha = LocalDateTime.now();
    List<ImagenPost> imagenes = new ArrayList<>();

    // Act
    Post post = Post.builder()
        .id(10L)
        .usuario(usuario)
        .titulo("Título de prueba")
        .descripcion("Descripción de prueba")
        .fechaCreacion(fecha)
        .tipoPost(TipoPost.POST)
        .linkVideo("https://youtube.com/test")
        .imagenes(imagenes)
        .build();

    // Assert
    assertEquals(10L, post.getId());
    assertEquals(usuario, post.getUsuario());
    assertEquals("Título de prueba", post.getTitulo());
    assertEquals("Descripción de prueba", post.getDescripcion());
    assertEquals(fecha, post.getFechaCreacion());
    assertEquals(TipoPost.POST, post.getTipoPost());
    assertEquals("https://youtube.com/test", post.getLinkVideo());
    assertEquals(imagenes, post.getImagenes());
  }

  @Test
  void testPostSettersYConstructores() {
    // Arrange
    Post post = new Post(); // Prueba del @NoArgsConstructor
    Usuario usuario = new Usuario();

    // Act
    post.setId(1L);
    post.setUsuario(usuario);
    post.setTitulo("Nuevo Título");
    post.setDescripcion("Nueva Descripción");
    post.setTipoPost(TipoPost.PREMIO);
    post.setLinkVideo(null);

    ImagenPost imagen = new ImagenPost();
    post.setImagenes(List.of(imagen));

    // Assert
    assertEquals(1L, post.getId());
    assertEquals("Nuevo Título", post.getTitulo());
    assertEquals("Nueva Descripción", post.getDescripcion());
    assertEquals(TipoPost.PREMIO, post.getTipoPost());
    assertNull(post.getLinkVideo());
    assertEquals(1, post.getImagenes().size());
  }

  @Test
  void testAllArgsConstructor() {
    // Arrange
    Usuario usuario = new Usuario();
    LocalDateTime fecha = LocalDateTime.now();
    List<ImagenPost> imagenes = new ArrayList<>();

    // Act - Prueba del @AllArgsConstructor
    Post post = new Post(1L, usuario, "Titulo", "Desc", fecha, TipoPost.POST, "link", imagenes);

    // Assert
    assertNotNull(post);
    assertEquals(1L, post.getId());
    assertEquals("Titulo", post.getTitulo());
  }
}