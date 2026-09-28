package edu.ies63.rcc2026_backend.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    // Clave secreta válida en Base64 de 256 bits (32 bytes)
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION_MS = 1000 * 60 * 60; // 1 hora

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION_MS);

        userDetails = new User("usuario@ejemplo.com", "password", Collections.emptyList());
    }

    @Test
    @DisplayName("Debe generar un token JWT y extraer el username correctamente")
    void generateToken_y_extractUsername() {
        Map<String, Object> claims = Map.of("rol", "USUARIO");

        String token = jwtService.generateToken(claims, userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("usuario@ejemplo.com");
    }

    @Test
    @DisplayName("Debe extraer el claim rol correctamente")
    void extractRol() {
        Map<String, Object> claims = Map.of("rol", "ORGANIZADOR");

        String token = jwtService.generateToken(claims, userDetails);

        assertThat(jwtService.extractRol(token)).isEqualTo("ORGANIZADOR");
    }

    @Test
    @DisplayName("Debe validar que el token es válido para el usuario que lo generó")
    void isTokenValid_Exito() {
        String token = jwtService.generateToken(Map.of(), userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Debe retornar falso si el usuario del token no coincide")
    void isTokenValid_UsuarioDiferente() {
        String token = jwtService.generateToken(Map.of(), userDetails);
        UserDetails otroUsuario = new User("otro@ejemplo.com", "password", Collections.emptyList());

        boolean isValid = jwtService.isTokenValid(token, otroUsuario);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Debe lanzar ExpiredJwtException cuando el token ha expirado")
    void isTokenExpired() {
        // Configuramos expiración negativa (-1000 ms)
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
        String expiredToken = jwtService.generateToken(Map.of(), userDetails);

        // Restauramos expiración normal
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION_MS);

        // JJWT arroja ExpiredJwtException al intentar leer un token vencido
        org.junit.jupiter.api.Assertions.assertThrows(
                io.jsonwebtoken.ExpiredJwtException.class,
                () -> jwtService.isTokenValid(expiredToken, userDetails)
        );
    }
}