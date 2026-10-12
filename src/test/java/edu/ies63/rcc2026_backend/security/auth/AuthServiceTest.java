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
import edu.ies63.rcc2026_backend.model.VerificationToken;
import edu.ies63.rcc2026_backend.repository.LocalidadRepository;
import edu.ies63.rcc2026_backend.repository.PasswordResetTokenRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.repository.VerificationTokenRepository;
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
import org.springframework.security.authentication.BadCredentialsException;
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
    private VerificationTokenRepository verificationTokenRepository;

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
                .verificado(true)
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

            // 1. Enseñarle al mock a validar la contraseña
            when(passwordEncoder.matches("Password123!", mockUser.getPassword())).thenReturn(true);

            // 2. El generador de JWT ahora recibe un Map (extraClaims), por lo que usamos anyMap() en vez de any()
            when(jwtService.generateToken(anyMap(), eq(mockUser))).thenReturn("jwt-token-generado");

            AuthResponse response = authService.authenticate(request);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("jwt-token-generado");
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("Debe lanzar BadCredentialsException si el usuario no existe")
        void authenticate_UsuarioNoExiste() {
            AuthRequest request = new AuthRequest("noexiste@ejemplo.com", "Password123!");
            when(userRepository.findByUsername("noexiste@ejemplo.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(BadCredentialsException.class) // Cambiado a BadCredentialsException
                .hasMessageContaining("Credenciales incorrectas"); // Cambiado al mensaje genérico
        }
    }

    @Nested
    @DisplayName("Pruebas de Registro")
    class RegisterTests {

        @Test
        @DisplayName("Debe registrar Usuario estándar correctamente sin generar JWT")
        void registerUsuario_Exito() {
            UsuarioCreateDTO request = mock(UsuarioCreateDTO.class);
            when(request.username()).thenReturn("juan@ejemplo.com");
            when(request.nombre()).thenReturn("Juan Perez");
            when(request.password()).thenReturn("Pass123!");
            when(request.localidadId()).thenReturn(1L);

            when(userRepository.findByUsername("juan@ejemplo.com")).thenReturn(Optional.empty());
            when(localidadRepository.findById(1L)).thenReturn(Optional.of(mockLocalidad));
            when(passwordEncoder.encode("Pass123!")).thenReturn("passCifrada");

            RegistroResponseDTO response = authService.registerUsuario(request);

            assertThat(response.username()).isEqualTo("juan@ejemplo.com");
            verify(userRepository, times(1)).save(any(Usuario.class));
            verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
            verify(emailService, times(1)).sendVerificationEmail(eq("juan@ejemplo.com"), anyString());
            verify(jwtService, never()).generateToken(anyMap(), any());
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

            RegistroResponseDTO response = authService.registerOrganizador(request);

            assertThat(response.username()).isEqualTo("org@ejemplo.com");
            verify(userRepository, times(1)).save(any(Usuario.class));
            verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
            verify(emailService, times(1)).sendVerificationEmail(eq("org@ejemplo.com"), anyString());
            verify(jwtService, never()).generateToken(anyMap(), any());
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

            RegistroResponseDTO response = authService.registerGrupo(request);

            assertThat(response.username()).isEqualTo("grupo@ejemplo.com");
            verify(userRepository, times(1)).save(any(Usuario.class));
            verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
            verify(emailService, times(1)).sendVerificationEmail(eq("grupo@ejemplo.com"), anyString());
            verify(jwtService, never()).generateToken(anyMap(), any());
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

    @Nested
    @DisplayName("Pruebas de verifyAccount")
    class VerifyAccountTests {

        @Test
        @DisplayName("Debe verificar cuenta correctamente y eliminar token")
        void verifyAccount_Exito() {
            String token = "token-valido";
            edu.ies63.rcc2026_backend.model.VerificationToken verificationToken = edu.ies63.rcc2026_backend.model.VerificationToken.builder()
                .token(token)
                .usuario(mockUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();

            when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));

            authService.verifyAccount(token);

            assertThat(mockUser.getVerificado()).isTrue();
            verify(userRepository).save(mockUser);
            verify(verificationTokenRepository).delete(verificationToken);
        }

        @Test
        @DisplayName("Debe lanzar ResponseStatusException si el token es inválido")
        void verifyAccount_TokenInvalido() {
            String token = "token-invalido";
            when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.verifyAccount(token))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Token inválido o no existe");
        }

        @Test
        @DisplayName("Debe lanzar ResponseStatusException si el token ha expirado")
        void verifyAccount_TokenExpirado() {
            String token = "token-expirado";
            edu.ies63.rcc2026_backend.model.VerificationToken verificationToken = edu.ies63.rcc2026_backend.model.VerificationToken.builder()
                .token(token)
                .usuario(mockUser)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .build();

            when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));

            assertThatThrownBy(() -> authService.verifyAccount(token))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("El token de verificación ha expirado");

            verify(verificationTokenRepository).delete(verificationToken);
            verify(userRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Debe lanzar ResponseStatusException si el usuario no está verificado")
    void authenticate_CuentaNoVerificada() {
        AuthRequest request = new AuthRequest("usuario@ejemplo.com", "Password123!");
        mockUser.setVerificado(false); // Simulamos que no verificó su correo

        when(userRepository.findByUsername("usuario@ejemplo.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("Password123!", mockUser.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> authService.authenticate(request))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Cuenta no verificada");

        verify(jwtService, never()).generateToken(anyMap(), any());
    }

    @Nested
    @DisplayName("Pruebas de resendVerificationEmail")
    class ResendVerificationTests {

        @Test
        @DisplayName("No hace nada si el usuario no existe o ya está verificado")
        void resend_UsuarioNoExisteOYaVerificado() {
            // Usuario no existe
            when(userRepository.findByUsername("inexistente@ejemplo.com")).thenReturn(Optional.empty());
            authService.resendVerificationEmail("inexistente@ejemplo.com");
            verify(verificationTokenRepository, never()).save(any());

            // Usuario ya verificado
            mockUser.setVerificado(true);
            when(userRepository.findByUsername("verificado@ejemplo.com")).thenReturn(Optional.of(mockUser));
            authService.resendVerificationEmail("verificado@ejemplo.com");
            verify(verificationTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Reenvía el correo si pasó el tiempo de spam (2 mins)")
        void resend_Exito_PasadoElTiempo() {
            mockUser.setVerificado(false);
            when(userRepository.findByUsername("usuario@ejemplo.com")).thenReturn(Optional.of(mockUser));

            VerificationToken tokenViejo = new VerificationToken();
            // Simulamos que se creó hace 5 minutos (es decir, expira en 24h menos 5 minutos)
            tokenViejo.setExpiryDate(LocalDateTime.now().plusHours(24).minusMinutes(5));
            when(verificationTokenRepository.findByUsuario(mockUser)).thenReturn(Optional.of(tokenViejo));

            authService.resendVerificationEmail("usuario@ejemplo.com");

            verify(verificationTokenRepository).deleteByUsuario(mockUser);
            verify(verificationTokenRepository).save(any(VerificationToken.class));
            verify(emailService).sendVerificationEmail(eq("usuario@ejemplo.com"), anyString());
        }

        @Test
        @DisplayName("Lanza TooManyRequests si se intenta reenviar antes de 2 minutos")
        void resend_Spam_LanzaExcepcion() {
            mockUser.setVerificado(false);
            when(userRepository.findByUsername("usuario@ejemplo.com")).thenReturn(Optional.of(mockUser));

            VerificationToken tokenReciente = new VerificationToken();
            // Simulamos que se creó hace 1 minuto (es decir, expira en 24h menos 1 minuto)
            tokenReciente.setExpiryDate(LocalDateTime.now().plusHours(24).minusMinutes(1));
            when(verificationTokenRepository.findByUsuario(mockUser)).thenReturn(Optional.of(tokenReciente));

            assertThatThrownBy(() -> authService.resendVerificationEmail("usuario@ejemplo.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("esperá al menos 2 minutos");

            verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
        }
    }
}