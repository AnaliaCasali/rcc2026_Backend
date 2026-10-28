package edu.ies63.rcc2026_backend.integration;

import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.enums.TipoPost;
import edu.ies63.rcc2026_backend.model.Post;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PostLikeIntegrationTest {

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

    @Autowired
    private JwtService jwtService;

    private Long postId;
    private String token;
    private Usuario usuario;

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

        // 1. Crear usuario que interactuará
        usuario = Usuario.builder()
                .username("usuario.like@test.com")
                .password("encoded_pass")
                .nombre("Usuario Tester")
                .role(Role.USUARIO)
                .debaja(false)
                .verificado(true)
                .build();
        usuario = usuarioRepository.save(usuario);

        // 2. Crear publicación destino
        Post post = new Post();
        post.setTitulo("Post para probar Likes");
        post.setDescripcion("Contenido relevante");
        post.setUsuario(usuario);

        // Asignar el tipo de post para evitar la restricción NOT NULL:
        post.setTipoPost(TipoPost.POST); // Si es un Enum TipoPost
        // Si tipoPost es un String: post.setTipoPost("TEXTO");

        post = postRepository.save(post);
        postId = post.getId();

        // 3. Generar token JWT para la sesión
        token = jwtService.generateToken(Map.of("rol", "USUARIO"), usuario);
    }

    @Test
    @DisplayName("Toggle Like: da me gusta en primera petición y lo remueve en la segunda")
    void toggleLike_CicloCompleto_PersisteEnBaseDeDatos() throws Exception {
        // --- 1. Dar Like (liked = true) ---
        mockMvc.perform(post("/api/v1/posts/" + postId + "/likes/toggle")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId", is(postId.intValue())))
                .andExpect(jsonPath("$.liked", is(true)))
                .andExpect(jsonPath("$.totalLikes", is(1)));

        // Verificación en base de datos real
        assertThat(postLikeRepository.countByPost_Id(postId)).isEqualTo(1L);
        assertThat(postLikeRepository.existsByPost_IdAndUsuario_Id(postId, usuario.getId())).isTrue();

        // --- 2. Quitar Like (liked = false) ---
        mockMvc.perform(post("/api/v1/posts/" + postId + "/likes/toggle")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId", is(postId.intValue())))
                .andExpect(jsonPath("$.liked", is(false)))
                .andExpect(jsonPath("$.totalLikes", is(0)));

        // Verificación en base de datos real
        assertThat(postLikeRepository.countByPost_Id(postId)).isEqualTo(0L);
        assertThat(postLikeRepository.existsByPost_IdAndUsuario_Id(postId, usuario.getId())).isFalse();
    }

    @Test
    @DisplayName("Toggle Like: retorna 404 si el post indicado no existe")
    void toggleLike_PostInexistente_Retorna404() throws Exception {
        mockMvc.perform(post("/api/v1/posts/999999/likes/toggle")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("Toggle Like: rechaza peticiones sin token JWT con 401/403")
    void toggleLike_SinAutenticacion_FallaSeguridad() throws Exception {
        mockMvc.perform(post("/api/v1/posts/" + postId + "/likes/toggle")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}