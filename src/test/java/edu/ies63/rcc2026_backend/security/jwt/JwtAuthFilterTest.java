package edu.ies63.rcc2026_backend.security.jwt;

import edu.ies63.rcc2026_backend.service.impl.UsuarioServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioServiceImpl userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("shouldNotFilter debe retornar true para rutas de la whitelist")
    void shouldNotFilter_WhiteList() {
        request.setRequestURI("/api/v1/auth/login");
        assertThat(jwtAuthFilter.shouldNotFilter(request)).isTrue();

        request.setRequestURI("/api/v1/auth/forgot-password");
        assertThat(jwtAuthFilter.shouldNotFilter(request)).isTrue();

        request.setRequestURI("/swagger-ui/index.html");
        assertThat(jwtAuthFilter.shouldNotFilter(request)).isTrue();

        request.setRequestURI("/api/v1/perfil");
        assertThat(jwtAuthFilter.shouldNotFilter(request)).isFalse();
    }

    @Test
    @DisplayName("Debe continuar la cadena si no se envía la cabecera Authorization")
    void doFilterInternal_SinCabecera() throws ServletException, IOException {
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Debe continuar la cadena si la cabecera no inicia con Bearer")
    void doFilterInternal_CabeceraInvalida() throws ServletException, IOException {
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Debe autenticar al usuario si el token es válido")
    void doFilterInternal_AutenticacionExitosa() throws ServletException, IOException {
        String token = "token.jwt.valido";
        String username = "usuario@ejemplo.com";
        UserDetails user = new User(username, "pass", Collections.emptyList());

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(user);
        when(jwtService.isTokenValid(token, user)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(username);
    }

    @Test
    @DisplayName("Debe responder 401 Unauthorized si el token está expirado")
    void doFilterInternal_TokenExpirado() throws ServletException, IOException {
        String token = "token.expirado";
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        when(jwtService.extractUsername(token)).thenThrow(new ExpiredJwtException(null, null, "Expirado"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Token expirado");
    }

    @Test
    @DisplayName("Debe responder 401 Unauthorized si el token es estructuralmente inválido")
    void doFilterInternal_TokenInvalido() throws ServletException, IOException {
        String token = "token.malformado";
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        when(jwtService.extractUsername(token)).thenThrow(new JwtException("Firma alterada"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Token inválido");
    }

}