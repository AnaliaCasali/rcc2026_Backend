package edu.ies63.rcc2026_backend.security.auth;

import edu.ies63.rcc2026_backend.dto.GrupoCreateDTO;
import edu.ies63.rcc2026_backend.dto.OrganizadorCreateDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioCreateDTO;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.exception.EmailYaRegistradoException;
import edu.ies63.rcc2026_backend.exception.PasswordInvalidaException;
import edu.ies63.rcc2026_backend.model.Localidad;
import edu.ies63.rcc2026_backend.model.PasswordResetToken;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.LocalidadRepository;
import edu.ies63.rcc2026_backend.repository.PasswordResetTokenRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.security.dto.*;
import edu.ies63.rcc2026_backend.security.jwt.JwtService;
import edu.ies63.rcc2026_backend.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository userRepository;

    @Mock
    private LocalidadRepository localidadRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordResetTokenRepository resetTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private Principal principal;

    @InjectMocks
    private AuthService authService;

    private Usuario mockUser;
    private Localidad mockLocalidad;

    @BeforeEach
    void setUp() {
        mockUser = Usuario.builder()
                .id(1L)
                .nombre("Usuario de Prueba")
                .username("usuario@ejemplo.com")
                .password("hashActual123")
                .role(Role.USUARIO)
                .debaja(false)
                .build();

        mockLocalidad = mock(Localidad.class);
    }

    @Nested
    @DisplayName("Pruebas de authenticate (Login)")
    class AuthenticateTests {

        @Test
        @DisplayName("Debe autenticar con éxito y devolver AuthResponse")
        void authenticate_Exito() {
            AuthRequest request = new AuthRequest("usuario@ejemplo.com", "Password123!");

            when(userRepository.findByUsername("usuario@ejemplo.com")).thenReturn(Optional.of(mockUser));
            when(jwtService.generateToken(any(), eq(mockUser))).thenReturn("jwt-token-generado");

            AuthResponse response = authService.authenticate(request);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("jwt-token-generado");
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).save(mockUser);
        }

        @Test
        @DisplayName("Debe lanzar ResponseStatusException si el usuario no existe")
        void authenticate_UsuarioNoExiste() {
            AuthRequest request = new AuthRequest("noexiste@ejemplo.com", "Password123!");

            when(userRepository.findByUsername("noexiste@ejemplo.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.authenticate(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Usuario no encontrado");
        }
    }

    @Nested
    @DisplayName("Pruebas de Registro")
    class RegisterTests {

        @Test
        @DisplayName("Debe registrar Usuario estándar correctamente")
        void registerUsuario_Exito() {
            UsuarioCreateDTO request = mock(UsuarioCreateDTO.class);
            when(request.username()).thenReturn("juan@ejemplo.com");
            when(request.nombre()).thenReturn("Juan Perez");
            when(request.password()).thenReturn("Pass123!");
            when(request.biografia()).thenReturn("Bio de prueba");
            when(request.localidadId()).thenReturn(1L);

            when(userRepository.findByUsername("juan@ejemplo.com")).thenReturn(Optional.empty());
            when(localidadRepository.findById(1L)).thenReturn(Optional.of(mockLocalidad));
            when(passwordEncoder.encode("Pass123!")).thenReturn("passCifrada");
            when(jwtService.generateToken(any(), any(Usuario.class))).thenReturn("token-jwt");

            AuthResponse response = authService.registerUsuario(request);

            assertThat(response.token()).isEqualTo("token-jwt");
            verify(userRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Debe registrar Organizador correctamente")
        void registerOrganizador_Exito() {
            OrganizadorCreateDTO request = mock(OrganizadorCreateDTO.class);
            when(request.username()).thenReturn("org@ejemplo.com");
            when(request.nombre()).thenReturn("Organización RCC");
            when(request.password()).thenReturn("Pass123!");
            when(request.biografia()).thenReturn("Bio org");
            when(request.director()).thenReturn("Director Responsable");
            when(request.localidadId()).thenReturn(1L);

            when(userRepository.findByUsername("org@ejemplo.com")).thenReturn(Optional.empty());
            when(localidadRepository.findById(1L)).thenReturn(Optional.of(mockLocalidad));
            when(passwordEncoder.encode("Pass123!")).thenReturn("passCifrada");
            when(jwtService.generateToken(any(), any(Usuario.class))).thenReturn("token-jwt");

            AuthResponse response = authService.registerOrganizador(request);

            assertThat(response.token()).isEqualTo("token-jwt");
            verify(userRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Debe registrar Grupo correctamente")
        void registerGrupo_Exito() {
            GrupoCreateDTO request = mock(GrupoCreateDTO.class);
            when(request.username()).thenReturn("grupo@ejemplo.com");
            when(request.nombre()).thenReturn("Grupo Coral");
            when(request.password()).thenReturn("Pass123!");
            when(request.biografia()).thenReturn("Bio grupo");
            when(request.director()).thenReturn("Director Coral");
            when(request.localidadId()).thenReturn(1L);

            when(userRepository.findByUsername("grupo@ejemplo.com")).thenReturn(Optional.empty());
            when(localidadRepository.findById(1L)).thenReturn(Optional.of(mockLocalidad));
            when(passwordEncoder.encode("Pass123!")).thenReturn("passCifrada");
            when(jwtService.generateToken(any(), any(Usuario.class))).thenReturn("token-jwt");

            AuthResponse response = authService.registerGrupo(request);

            assertThat(response.token()).isEqualTo("token-jwt");
            verify(userRepository).save(any(Usuario.class));
        }

        @Test
        @DisplayName("Debe lanzar EmailYaRegistradoException si el username ya está tomado")
        void register_UsernameDuplicado() {
            UsuarioCreateDTO request = mock(UsuarioCreateDTO.class);
            when(request.username()).thenReturn("usuario@ejemplo.com");

            when(userRepository.findByUsername("usuario@ejemplo.com")).thenReturn(Optional.of(mockUser));

            assertThatThrownBy(() -> authService.registerUsuario(request))
                    .isInstanceOf(EmailYaRegistradoException.class)
                    .hasMessageContaining("El usuario ya existe");

            verify(userRepository, never()).save(any(Usuario.class));
        }
    }

    @Nested
    @DisplayName("Pruebas de cambiarPassword")
    class CambiarPasswordTests {

        @Test
        @DisplayName("Debe cambiar la contraseña con éxito cuando todo es válido")
        void cambiarPassword_Exito() {
            CambiarPasswordRequest request = new CambiarPasswordRequest(
                    "PasswordVieja123",
                    "PasswordNueva456",
                    "PasswordNueva456"
            );

            when(principal.getName()).thenReturn("usuario@ejemplo.com");
            when(userRepository.findByUsername("usuario@ejemplo.com")).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.matches("PasswordVieja123", "hashActual123")).thenReturn(true);
            when(passwordEncoder.matches("PasswordNueva456", "hashActual123")).thenReturn(false);
            when(passwordEncoder.encode("PasswordNueva456")).thenReturn("nuevoHashBCrypt");

            authService.cambiarPassword(request, principal);

            assertThat(mockUser.getPassword()).isEqualTo("nuevoHashBCrypt");
            verify(userRepository).save(mockUser);
        }

        @Test
        @DisplayName("Debe lanzar excepción si el usuario no existe")
        void cambiarPassword_UsuarioNoEncontrado() {
            CambiarPasswordRequest request = new CambiarPasswordRequest(
                    "PasswordVieja123",
                    "PasswordNueva456",
                    "PasswordNueva456"
            );

            when(principal.getName()).thenReturn("inexistente@ejemplo.com");
            when(userRepository.findByUsername("inexistente@ejemplo.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.cambiarPassword(request, principal))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Usuario no encontrado");
        }

        @Test
        @DisplayName("Debe lanzar PasswordInvalidaException si la contraseña actual no es correcta")
        void cambiarPassword_ActualIncorrecta() {
            CambiarPasswordRequest request = new CambiarPasswordRequest(
                    "PasswordErronea",
                    "PasswordNueva456",
                    "PasswordNueva456"
            );

            when(principal.getName()).thenReturn("usuario@ejemplo.com");
            when(userRepository.findByUsername("usuario@ejemplo.com")).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.matches("PasswordErronea", "hashActual123")).thenReturn(false);

            assertThatThrownBy(() -> authService.cambiarPassword(request, principal))
                    .isInstanceOf(PasswordInvalidaException.class)
                    .hasMessageContaining("La contraseña actual no es correcta");
        }

        @Test
        @DisplayName("Debe lanzar PasswordInvalidaException si la confirmación no coincide")
        void cambiarPassword_ConfirmacionNoCoincide() {
            CambiarPasswordRequest request = new CambiarPasswordRequest(
                    "PasswordVieja123",
                    "PasswordNueva456",
                    "PasswordDistinta789"
            );

            when(principal.getName()).thenReturn("usuario@ejemplo.com");
            when(userRepository.findByUsername("usuario@ejemplo.com")).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.matches("PasswordVieja123", "hashActual123")).thenReturn(true);

            assertThatThrownBy(() -> authService.cambiarPassword(request, principal))
                    .isInstanceOf(PasswordInvalidaException.class)
                    .hasMessageContaining("La nueva contraseña y su confirmación no coinciden");
        }

        @Test
        @DisplayName("Debe lanzar PasswordInvalidaException si la nueva contraseña es igual a la actual")
        void cambiarPassword_MismaContraseña() {
            CambiarPasswordRequest request = new CambiarPasswordRequest(
                    "PasswordVieja123",
                    "PasswordVieja123",
                    "PasswordVieja123"
            );

            when(principal.getName()).thenReturn("usuario@ejemplo.com");
            when(userRepository.findByUsername("usuario@ejemplo.com")).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.matches("PasswordVieja123", "hashActual123")).thenReturn(true);

            assertThatThrownBy(() -> authService.cambiarPassword(request, principal))
                    .isInstanceOf(PasswordInvalidaException.class)
                    .hasMessageContaining("La nueva contraseña no puede ser igual a la actual");
        }
    }

    @Nested
    @DisplayName("Pruebas de requestPasswordReset (Olvido de contraseña)")
    class ForgotPasswordTests {

        @Test
        @DisplayName("Debe generar token y enviar correo cuando el usuario existe")
        void requestPasswordReset_UsuarioExiste() {
            ForgotPasswordRequest request = new ForgotPasswordRequest("usuario@ejemplo.com");
            when(userRepository.findByUsername("usuario@ejemplo.com")).thenReturn(Optional.of(mockUser));

            authService.requestPasswordReset(request);

            verify(resetTokenRepository).deleteByUsuario(mockUser);
            ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
            verify(resetTokenRepository).save(tokenCaptor.capture());

            PasswordResetToken savedToken = tokenCaptor.getValue();
            assertThat(savedToken.getToken()).isNotBlank();
            assertThat(savedToken.getUsuario()).isEqualTo(mockUser);
            assertThat(savedToken.getExpiryDate()).isAfter(LocalDateTime.now());

            verify(emailService).sendPasswordResetEmail(eq("usuario@ejemplo.com"), eq(savedToken.getToken()));
        }

        @Test
        @DisplayName("No debe enviar correo ni crear token si el usuario no existe")
        void requestPasswordReset_UsuarioNoExiste() {
            ForgotPasswordRequest request = new ForgotPasswordRequest("desconocido@ejemplo.com");
            when(userRepository.findByUsername("desconocido@ejemplo.com")).thenReturn(Optional.empty());

            authService.requestPasswordReset(request);

            verify(resetTokenRepository, never()).save(any());
            verify(emailService, never()).sendPasswordResetEmail(any(), any());
        }
    }

    @Nested
    @DisplayName("Pruebas de resetPassword")
    class ResetPasswordTests {

        @Test
        @DisplayName("Debe actualizar contraseña y eliminar el token usado con éxito")
        void resetPassword_Exito() {
            String tokenValue = "token-recuperacion-uuid";
            ResetPasswordRequest request = new ResetPasswordRequest(
                    tokenValue,
                    "NuevaPass123!",
                    "NuevaPass123!"
            );

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(tokenValue)
                    .usuario(mockUser)
                    .expiryDate(LocalDateTime.now().plusMinutes(10))
                    .build();

            when(resetTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(resetToken));
            when(passwordEncoder.encode("NuevaPass123!")).thenReturn("nuevoHashCifrado");

            authService.resetPassword(request);

            assertThat(mockUser.getPassword()).isEqualTo("nuevoHashCifrado");
            verify(userRepository).save(mockUser);
            verify(resetTokenRepository).delete(resetToken);
        }

        @Test
        @DisplayName("Debe lanzar ResponseStatusException si las contraseñas no coinciden")
        void resetPassword_ContraseñasNoCoinciden() {
            ResetPasswordRequest request = new ResetPasswordRequest(
                    "token-uuid",
                    "Password123!",
                    "Diferente123!"
            );

            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Las contraseñas no coinciden");
        }

        @Test
        @DisplayName("Debe lanzar ResponseStatusException si el token no existe")
        void resetPassword_TokenNoExiste() {
            ResetPasswordRequest request = new ResetPasswordRequest(
                    "token-inexistente",
                    "Password123!",
                    "Password123!"
            );

            when(resetTokenRepository.findByToken("token-inexistente")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Token de recuperación inválido o inexistente");
        }

        @Test
        @DisplayName("Debe eliminar el token y lanzar ResponseStatusException si el token expiró")
        void resetPassword_TokenExpirado() {
            String tokenValue = "token-vencido";
            ResetPasswordRequest request = new ResetPasswordRequest(
                    tokenValue,
                    "Password123!",
                    "Password123!"
            );

            PasswordResetToken expiredToken = PasswordResetToken.builder()
                    .token(tokenValue)
                    .usuario(mockUser)
                    .expiryDate(LocalDateTime.now().minusMinutes(1))
                    .build();

            when(resetTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(expiredToken));

            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("El token de recuperación ha expirado");

            verify(resetTokenRepository).delete(expiredToken);
            verify(userRepository, never()).save(any());
        }
    }

}