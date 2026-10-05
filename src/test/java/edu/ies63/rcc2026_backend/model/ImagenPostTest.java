package edu.ies63.rcc2026_backend.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ImagenPostTest {

  @Test
  void testImagenPostBuilderYGetters() {
    // Arrange
    Post postMock = new Post();
    postMock.setId(5L);

    // Act
    ImagenPost imagen = ImagenPost.builder()
        .id(1L)
        .url("/uploads/posts/test.webp")
        .alt("Texto alternativo")
        .post(postMock)
        .build();

    // Assert
    assertEquals(1L, imagen.getId());
    assertEquals("/uploads/posts/test.webp", imagen.getUrl());
    assertEquals("Texto alternativo", imagen.getAlt());
    assertEquals(postMock, imagen.getPost());
    assertEquals(5L, imagen.getPost().getId());
  }

  @Test
  void testImagenPostSettersYConstructores() {
    // Arrange
    ImagenPost imagen = new ImagenPost(); // @NoArgsConstructor
    Post postMock = new Post();

    // Act
    imagen.setId(2L);
    imagen.setUrl("/ruta/imagen.jpg");
    imagen.setAlt("Alt de prueba");
    imagen.setPost(postMock);

    // Assert
    assertEquals(2L, imagen.getId());
    assertEquals("/ruta/imagen.jpg", imagen.getUrl());
    assertEquals("Alt de prueba", imagen.getAlt());
    assertNotNull(imagen.getPost());
  }

  @Test
  void testAllArgsConstructor() {
    Post post = new Post();

    ImagenPost imagen = new ImagenPost(10L, "/url/test.png", "Imagen alt", post);

    assertNotNull(imagen);
    assertEquals(10L, imagen.getId());
    assertEquals("/url/test.png", imagen.getUrl());
    assertEquals("Imagen alt", imagen.getAlt());
  }
}