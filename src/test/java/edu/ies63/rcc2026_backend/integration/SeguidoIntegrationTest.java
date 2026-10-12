package edu.ies63.rcc2026_backend.integration;

import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.EventoRepository;
import edu.ies63.rcc2026_backend.repository.GrupoEventoRepository;
import edu.ies63.rcc2026_backend.repository.LocalidadRepository;
import edu.ies63.rcc2026_backend.repository.NotificacionRepository;
import edu.ies63.rcc2026_backend.repository.PasswordResetTokenRepository;
import edu.ies63.rcc2026_backend.repository.PostLikeRepository;
import edu.ies63.rcc2026_backend.repository.PostRepository;
import edu.ies63.rcc2026_backend.repository.SeguidoRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.repository.VerificationTokenRepository;
import edu.ies63.rcc2026_backend.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SeguidoIntegrationTest {
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

  @Autowired private MockMvc mockMvc;

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private LocalidadRepository localidadRepository;

  @Autowired
  private NotificacionRepository notificacionRepository;

  @Autowired
  private GrupoEventoRepository grupoEventoRepository;

  @Autowired
  private EventoRepository eventoRepository;

  @Autowired
  private PostLikeRepository postLikeRepository;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private SeguidoRepository seguidoRepository;

  @Autowired
  private VerificationTokenRepository verificationTokenRepository;

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Autowired private JwtService jwtService;

  private Usuario seguidor;
  private Usuario grupo;
  private String token;

  @BeforeEach
  void setUp() {
    notificacionRepository.deleteAll();
    grupoEventoRepository.deleteAll();
    eventoRepository.deleteAll();
    postLikeRepository.deleteAll();
    postRepository.deleteAll();
    seguidoRepository.deleteAll();
    verificationTokenRepository.deleteAll();
    passwordResetTokenRepository.deleteAll();

    usuarioRepository.deleteAll();
    localidadRepository.deleteAll();

    seguidor = Usuario.builder()
        .username("fan@test.com")
        .password("hash")
        .nombre("Fanático")
        .role(Role.USUARIO)
        .verificado(true)
        .build();

    grupo = Usuario.builder()
        .username("banda@test.com")
        .password("hash")
        .nombre("La Banda")
        .role(Role.GRUPO)
        .verificado(true)
        .build();

    usuarioRepository.save(seguidor);
    usuarioRepository.save(grupo);

    token = jwtService.generateToken(Map.of("rol", "USUARIO"), seguidor);
  }

  @Test
  @DisplayName("POST toggle seguimiento alterna el estado correctamente")
  void toggleSeguimiento_CreaYElimina() throws Exception {
    // Primera vez: Sigue
    mockMvc.perform(post("/api/v1/seguidos/" + grupo.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.siguiendo", is(true)))
        .andExpect(jsonPath("$.seguidores", is(1)));

    assertThat(seguidoRepository.countByGrupoId(grupo.getId())).isEqualTo(1L);

    // Segunda vez: Deja de seguir
    mockMvc.perform(post("/api/v1/seguidos/" + grupo.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.siguiendo", is(false)))
        .andExpect(jsonPath("$.seguidores", is(0)));

    assertThat(seguidoRepository.countByGrupoId(grupo.getId())).isEqualTo(0L);
  }

  @Test
  @DisplayName("GET estadoSeguimiento retorna estado actual")
  void estadoSeguimiento_RetornaEstado() throws Exception {
    mockMvc.perform(get("/api/v1/seguidos/" + grupo.getId() + "/estado")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.siguiendo", is(false)))
        .andExpect(jsonPath("$.seguidores", is(0)));
  }
}