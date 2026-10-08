package edu.ies63.rcc2026_backend.model;

import edu.ies63.rcc2026_backend.enums.Plataforma;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RedSocialTest {
  @Test
  void testBuilderYGetters() {
    Usuario user = new Usuario();
    user.setId(1L);

    RedSocial red = RedSocial.builder()
        .id(1L)
        .plataforma(Plataforma.INSTAGRAM)
        .url("https://instagram.com/test")
        .nombreUsuario("@test")
        .usuario(user)
        .build();

    assertThat(red.getId()).isEqualTo(1L);
    assertThat(red.getPlataforma()).isEqualTo(Plataforma.INSTAGRAM);
    assertThat(red.getUrl()).isEqualTo("https://instagram.com/test");
    assertThat(red.getNombreUsuario()).isEqualTo("@test");
    assertThat(red.getUsuario()).isNotNull();
  }
}