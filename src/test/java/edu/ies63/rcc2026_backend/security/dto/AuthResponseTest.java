package edu.ies63.rcc2026_backend.security.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthResponseTest {

    @Test
    @DisplayName("Debe almacenar y retornar el token JWT")
    void testCreacionYGetters() {
        AuthResponse dto = new AuthResponse("mocked.jwt.token");

        assertThat(dto.token()).isEqualTo("mocked.jwt.token");
        assertThat(dto.toString()).contains("mocked.jwt.token");
    }

    @Test
    @DisplayName("Debe validar equals y hashCode")
    void testEqualsYHashCode() {
        AuthResponse dto1 = new AuthResponse("token123");
        AuthResponse dto2 = new AuthResponse("token123");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

}