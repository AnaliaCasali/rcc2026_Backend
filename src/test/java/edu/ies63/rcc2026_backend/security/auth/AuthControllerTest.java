package edu.ies63.rcc2026_backend.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ies63.rcc2026_backend.dto.GrupoCreateDTO;
import edu.ies63.rcc2026_backend.dto.OrganizadorCreateDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioCreateDTO;
import edu.ies63.rcc2026_backend.security.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login responde 200 OK con AuthResponse")
    void login_Exito() throws Exception {
        AuthRequest request = new AuthRequest("usuario@ejemplo.com", "Password123!");
        AuthResponse response = new AuthResponse("mocked-jwt-token");

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    @DisplayName("PATCH /api/v1/auth/cambiarpassword responde 204 No Content")
    void cambiarPassword_Exito() throws Exception {
        CambiarPasswordRequest request = new CambiarPasswordRequest(
                "PassActual123",
                "PassNueva456",
                "PassNueva456"
        );

        doNothing().when(authService).cambiarPassword(eq(request), any(Principal.class));

        mockMvc.perform(patch("/api/v1/auth/cambiarpassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(() -> "usuario@ejemplo.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register/usuario responde 201 Created")
    void registerUsuario_Exito() throws Exception {
        UsuarioCreateDTO request = new UsuarioCreateDTO(
                "juan@ejemplo.com",
                "Juan Perez",
                "Password123!",
                "Bio de prueba",
                1L
        );
        AuthResponse response = new AuthResponse("mocked-jwt-token");

        when(authService.registerUsuario(any(UsuarioCreateDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register/organizador responde 201 Created")
    void registerOrganizador_Exito() throws Exception {
        OrganizadorCreateDTO request = new OrganizadorCreateDTO(
                "org@ejemplo.com",
                "Organización RCC",
                "Password123!",
                "Bio org",
                "Director Responsable",
                1L
        );
        AuthResponse response = new AuthResponse("mocked-jwt-token");

        when(authService.registerOrganizador(any(OrganizadorCreateDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register/organizador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register/grupo responde 201 Created")
    void registerGrupo_Exito() throws Exception {
        GrupoCreateDTO request = new GrupoCreateDTO(
                "grupo@ejemplo.com",
                "Grupo Coral",
                "Password123!",
                "Bio grupo",
                "Director Coral",
                1L
        );
        AuthResponse response = new AuthResponse("mocked-jwt-token");

        when(authService.registerGrupo(any(GrupoCreateDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register/grupo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password responde 200 OK")
    void forgotPassword_Exito() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("usuario@ejemplo.com");
        doNothing().when(authService).requestPasswordReset(any(ForgotPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password responde 200 OK")
    void resetPassword_Exito() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest(
                "token-uuid-1234",
                "NuevaClave123!",
                "NuevaClave123!"
        );
        doNothing().when(authService).resetPassword(any(ResetPasswordRequest.class));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}