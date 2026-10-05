package edu.ies63.rcc2026_backend.integration;

import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.enums.TipoPost;
import edu.ies63.rcc2026_backend.model.ImagenPost;
import edu.ies63.rcc2026_backend.model.Post;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.PostRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.security.jwt.JwtService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
public class PostIntegrationTest {

  @Container
  static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("test_rcc2026")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
    registry.add("spring.datasource.username", mysqlContainer::getUsername);
    registry.add("spring.datasource.password", mysqlContainer::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
    registry.add("spring.datasource.hikari.maximum-pool-size", () -> "2");
    registry.add("app.security.jwt.secret", () -> "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
    registry.add("app.security.jwt.expiration", () -> "86400000");
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UsuarioRepository usuarioRepository;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtService jwtService;

  private String jwtTokenValid;
  private Usuario usuarioActual;

  @BeforeEach
  void setUp() {
    Usuario usuarioTest = Usuario.builder()
        .username("test@correo.com")
        .password(passwordEncoder.encode("password123"))
        .nombre("Usuario de Prueba")
        .role(Role.USUARIO)
        .debaja(false)
        .build();
    usuarioActual = usuarioRepository.save(usuarioTest);

    Map<String, Object> claims = new HashMap<>();
    claims.put("rol", usuarioActual.getRole().name());
    claims.put("id", usuarioActual.getId());
    claims.put("name", usuarioActual.getNombre());

    jwtTokenValid = jwtService.generateToken(claims, usuarioActual);
  }

  // --- MÉTODOS AUXILIARES ---
  private Post crearPostDirectoEnBD() {
    Post post = Post.builder()
        .titulo("Post Original")
        .descripcion("Descripción inicial")
        .tipoPost(TipoPost.POST)
        .usuario(usuarioActual)
        .build();

    ImagenPost imagen = ImagenPost.builder()
        .url("/uploads/posts/imagen_falsa.webp")
        .alt("Imagen Original")
        .post(post)
        .build();

    post.setImagenes(new ArrayList<>(List.of(imagen)));
    return postRepository.saveAndFlush(post);
  }

  // --- TESTS ---

  @Test
  public void listarPosts_DeberiaRetornarStatus200_CuandoEsPublico() throws Exception {
    mockMvc.perform(get("/api/v1/posts")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").exists());
  }

  @Test
  public void crearPost_DeberiaRetornar201_CuandoTokenEsValido() throws Exception {
    java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    javax.imageio.ImageIO.write(bufferedImage, "jpg", baos);
    byte[] imageBytes = baos.toByteArray();

    MockMultipartFile imagenMock = new MockMultipartFile(
        "imagenes",
        "foto.jpg",
        "image/jpeg",
        imageBytes
    );

    mockMvc.perform(multipart("/api/v1/posts/crear")
            .file(imagenMock)
            .param("titulo", "Título de Integración")
            .param("descripcion", "Prueba desde Testcontainers")
            .param("tipoPost", TipoPost.POST.name())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTokenValid)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.titulo").value("Título de Integración"))
        .andExpect(jsonPath("$.usuarioNombre").value("Usuario de Prueba"));
  }

  @Test
  public void editarPost_DeberiaRetornar200_CuandoDatosSonValidos() throws Exception {
    Post postGuardado = crearPostDirectoEnBD();

    // Para peticiones PATCH que envían multipart/form-data, se usa multipart()
    // pero se sobreescribe el método HTTP a PATCH internamente usando .with()
    mockMvc.perform(multipart("/api/v1/posts/editar/" + postGuardado.getId())
            .param("titulo", "Título Editado")
            .param("descripcion", "Descripción editada")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTokenValid)
            .with(request -> {
              request.setMethod("PATCH");
              return request;
            })
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.titulo").value("Título Editado"))
        .andExpect(jsonPath("$.descripcion").value("Descripción editada"));
  }

  @Test
  public void eliminarPost_DeberiaRetornar204_CuandoSeEliminaCorrectamente() throws Exception {
    Post postGuardado = crearPostDirectoEnBD();

    mockMvc.perform(delete("/api/v1/posts/eliminar/" + postGuardado.getId())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTokenValid))
        .andExpect(status().isNoContent());

    // Verificamos que ya no exista en la BD
    Assertions.assertTrue(postRepository.findById(postGuardado.getId()).isEmpty());
  }
}