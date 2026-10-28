package edu.ies63.rcc2026_backend.integration;

import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Notificacion;
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

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class NotificacionIntegrationTest {
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

  private String token;
  private Usuario usuario;
  private Notificacion notificacion;

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

    usuario = Usuario.builder()
        .username("usuario.noti@test.com")
        .password("hash")
        .nombre("Usuario Notificaciones")
        .role(Role.USUARIO)
        .debaja(false)
        .verificado(true)
        .build();
    usuario = usuarioRepository.save(usuario);

    notificacion = Notificacion.builder()
        .perfil(usuario)
        .mensaje("Nueva invitación a evento")
        .leida(false)
        .fechaCreacion(LocalDateTime.now())
        .eventoId(100L)
        .build();
    notificacion = notificacionRepository.save(notificacion);

    token = jwtService.generateToken(Map.of("rol", "USUARIO"), usuario);
  }

  @Test
  @DisplayName("GET /api/v1/notificaciones retorna las notificaciones del usuario")
  void listarMias_RetornaNotificaciones() throws Exception {
    mockMvc.perform(get("/api/v1/notificaciones")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].mensaje", is("Nueva invitación a evento")))
        .andExpect(jsonPath("$[0].leida", is(false)));
  }

  @Test
  @DisplayName("GET /api/v1/notificaciones/no-leidas retorna la cantidad")
  void contarNoLeidas_RetornaCantidad() throws Exception {
    mockMvc.perform(get("/api/v1/notificaciones/no-leidas/count")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.count", is(1)));
  }

  @Test
  @DisplayName("PATCH /api/v1/notificaciones/{id}/leida actualiza estado a leída")
  void marcarLeida_CambiaEstadoYRetorna204() throws Exception {
    mockMvc.perform(patch("/api/v1/notificaciones/" + notificacion.getId() + "/leer")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isNoContent());

    Notificacion actualizada = notificacionRepository.findById(notificacion.getId()).orElseThrow();
    assertThat(actualizada.getLeida()).isTrue();
  }
}