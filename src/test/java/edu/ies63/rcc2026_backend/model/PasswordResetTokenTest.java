package edu.ies63.rcc2026_backend.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordResetTokenTest {

    @Test
    @DisplayName("Debe instanciar correctamente mediante Builder y verificar getters")
    void testBuilderYGetters() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .username("usuario@ejemplo.com")
                .build();

        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken token = PasswordResetToken.builder()
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
    @DisplayName("Debe permitir instanciación vacía y asignación por setters")
    void testNoArgsConstructorYSetters() {
        PasswordResetToken token = new PasswordResetToken();
        Usuario usuario = new Usuario();
        LocalDateTime fecha = LocalDateTime.now().plusHours(1);

        token.setId(20L);
        token.setToken("nuevo-token");
        token.setUsuario(usuario);
        token.setExpiryDate(fecha);

        assertThat(token.getId()).isEqualTo(20L);
        assertThat(token.getToken()).isEqualTo("nuevo-token");
        assertThat(token.getUsuario()).isEqualTo(usuario);
        assertThat(token.getExpiryDate()).isEqualTo(fecha);
    }

    @Test
    @DisplayName("isExpired debe retornar false si la fecha de expiración está en el futuro")
    void isExpired_TokenVigente_RetornaFalse() {
        PasswordResetToken token = PasswordResetToken.builder()
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .build();

        assertThat(token.isExpired()).isFalse();
    }

    @Test
    @DisplayName("isExpired debe retornar true si la fecha de expiración está en el pasado")
    void isExpired_TokenVencido_RetornaTrue() {
        PasswordResetToken token = PasswordResetToken.builder()
                .expiryDate(LocalDateTime.now().minusMinutes(5))
                .build();

        assertThat(token.isExpired()).isTrue();
    }
}