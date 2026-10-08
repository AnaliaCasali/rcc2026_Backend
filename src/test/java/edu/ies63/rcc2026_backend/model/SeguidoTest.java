package edu.ies63.rcc2026_backend.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SeguidoTest {
  @Test
  void testBuilderYGetters() {
    Usuario user = new Usuario(); user.setId(1L);
    Usuario grupo = new Usuario(); grupo.setId(2L);

    Seguido seguido = Seguido.builder()
        .id(1L)
        .usuario(user)
        .grupo(grupo)
        .build();

    assertThat(seguido.getId()).isEqualTo(1L);
    assertThat(seguido.getUsuario().getId()).isEqualTo(1L);
    assertThat(seguido.getGrupo().getId()).isEqualTo(2L);
  }
}