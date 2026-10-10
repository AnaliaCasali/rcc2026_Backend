package edu.ies63.rcc2026_backend.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationTokenTest {

  @Test
  @DisplayName("Debe instanciar correctamente mediante Builder y verificar getters")
  void testBuilderYGetters() {
    Usuario usuario = Usuario.builder().id(1L).username("test@test.com").build();
    LocalDateTime expiracion = LocalDateTime.now().plusHours(24);

    VerificationToken token = VerificationToken.builder()
        .id(10L)
        .token("uuid-token-1234")
        .usuario(usuario)
        .expiryDate(expiracion)
        .build();

    assertThat(token.getId()).isEqualTo(10L);
    assertThat(token.getToken()).isEqualTo("uuid-token-1234");
    assertThat(token.getUsuario()).isEqualTo(usuario);
    assertThat(token.getExpiryDate()).isEqualTo(expiracion);
  }

  @Test
  @DisplayName("isExpired debe retornar false si la fecha de expiración está en el futuro")
  void isExpired_TokenVigente_RetornaFalse() {
    VerificationToken token = VerificationToken.builder()
        .expiryDate(LocalDateTime.now().plusMinutes(10))
        .build();
    assertThat(token.isExpired()).isFalse();
  }

  @Test
  @DisplayName("isExpired debe retornar true si la fecha de expiración está en el pasado")
  void isExpired_TokenVencido_RetornaTrue() {
    VerificationToken token = VerificationToken.builder()
        .expiryDate(LocalDateTime.now().minusMinutes(5))
        .build();
    assertThat(token.isExpired()).isTrue();
  }
}