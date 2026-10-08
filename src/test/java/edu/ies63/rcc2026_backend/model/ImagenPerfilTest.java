package edu.ies63.rcc2026_backend.model;

import edu.ies63.rcc2026_backend.enums.TipoImagen;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ImagenPerfilTest {
  @Test
  void testBuilderYGetters() {
    Usuario user = new Usuario(); user.setId(1L);

    ImagenPerfil img = ImagenPerfil.builder()
        .id(1L)
        .url("/url")
        .alt("alt")
        .tipoImagen(TipoImagen.FOTOPERFIL)
        .usuario(user)
        .build();

    assertThat(img.getId()).isEqualTo(1L);
    assertThat(img.getUrl()).isEqualTo("/url");
    assertThat(img.getTipoImagen()).isEqualTo(TipoImagen.FOTOPERFIL);
    assertThat(img.getUsuario()).isNotNull();
  }
}