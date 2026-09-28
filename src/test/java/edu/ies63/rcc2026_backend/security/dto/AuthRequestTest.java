package edu.ies63.rcc2026_backend.security.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRequestTest {

    @Test
    @DisplayName("Debe instanciar y acceder a las propiedades correctamente")
    void testCreacionYGetters() {
        AuthRequest dto = new AuthRequest("usuario@ejemplo.com", "Password123!");

        assertThat(dto.username()).isEqualTo("usuario@ejemplo.com");
        assertThat(dto.password()).isEqualTo("Password123!");
        assertThat(dto.toString()).contains("usuario@ejemplo.com");
    }

    @Test
    @DisplayName("Debe validar equals y hashCode en records equivalentes")
    void testEqualsYHashCode() {
        AuthRequest dto1 = new AuthRequest("user@test.com", "123456");
        AuthRequest dto2 = new AuthRequest("user@test.com", "123456");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

}