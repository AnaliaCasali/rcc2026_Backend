package edu.ies63.rcc2026_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.PasswordResetTokenRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.security.dto.AuthRequest;
import edu.ies63.rcc2026_backend.security.dto.CambiarPasswordRequest;
import edu.ies63.rcc2026_backend.security.dto.ForgotPasswordRequest;
import edu.ies63.rcc2026_backend.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class AuthIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("rcc_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // resetTokenRepository.deleteAll();
        // usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login autentica credenciales reales y retorna JWT")
    void login_AutenticaCredencialesReales() throws Exception {
        Usuario usuario = Usuario.builder()
                .username("login@test.com")
                .password(passwordEncoder.encode("Password123!"))
                .nombre("Usuario Login")
                .role(Role.USUARIO)
                .debaja(false)
                .verificado(true)
                .build();
        usuarioRepository.save(usuario);

        AuthRequest request = new AuthRequest("login@test.com", "Password123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password es público y no exige token")
    void forgotPassword_EndpointPublico() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("noexiste@test.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Autowired
    private edu.ies63.rcc2026_backend.repository.VerificationTokenRepository verificationTokenRepository;

    @Test
    @DisplayName("POST /api/v1/auth/reset-password responde bad request si token es inválido")
    void resetPassword_TokenInvalido() throws Exception {
        edu.ies63.rcc2026_backend.security.dto.ResetPasswordRequest request =
            new edu.ies63.rcc2026_backend.security.dto.ResetPasswordRequest("token-falso", "ClaveValida123!", "ClaveValida123!");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("inválido o inexistente")));
    }

    @Test
    @DisplayName("GET /api/v1/auth/verify-account valida el token")
    void verifyAccount_ValidaToken() throws Exception {
        // En una integración real se espera 400 si pasamos un token inventado sin crearlo antes en BD
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/auth/verify-account")
                .param("token", "token-inventado"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", org.hamcrest.Matchers.containsString("inválido o no existe")));
    }
}