package edu.ies63.rcc2026_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ies63.rcc2026_backend.dto.UsuarioCreateDTO;
import edu.ies63.rcc2026_backend.enums.Pais;
import edu.ies63.rcc2026_backend.enums.Provincia;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Localidad;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.LocalidadRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UsuarioIntegrationTest {

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
  private JwtService jwtService;

  // Instancia directa para evitar errores de inyección
  private final ObjectMapper objectMapper = new ObjectMapper();

  private Localidad localidadTest;

  @BeforeEach
  void setUp() {
    usuarioRepository.deleteAll();
    localidadRepository.deleteAll();

    localidadTest = new Localidad();
    localidadTest.setLocalidad("Resistencia");
    localidadTest.setProvincia(Provincia.CHACO);
    localidadTest.setPais(Pais.ARGENTINA);
    localidadTest = localidadRepository.save(localidadTest);
  }

  @Test
  void debeRegistrarUnUsuarioCorrectamente() throws Exception {
    UsuarioCreateDTO request = new UsuarioCreateDTO(
        "nuevo@test.com", "secreta123", "Usuario Integracion", "Bio Test", localidadTest.getId()
    );

    mockMvc.perform(post("/api/v1/auth/register/usuario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").exists());
  }

  @Test
  void debeDevolverMiPerfilAutenticado() throws Exception {
    // 1. Guardamos el usuario en la BD de prueba
    Usuario usuario = Usuario.builder()
        .username("integracion@test.com")
        .password("hash")
        .nombre("Mi Perfil Integracion")
        .role(Role.USUARIO)
        .localidad(localidadTest)
        .build();
    usuarioRepository.save(usuario);

    // 2. Generamos el token real firmado
    String token = jwtService.generateToken(Map.of("rol", "USUARIO"), usuario);

    // 3. Pasamos el token en la cabecera Authorization
    mockMvc.perform(get("/api/v1/auth/usuarios/perfil/mi-perfil")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username", is("integracion@test.com")))
        .andExpect(jsonPath("$.nombre", is("Mi Perfil Integracion")));
  }

  @Test
  void debeEditarElPerfilSoportandoMultipart() throws Exception {
    Usuario usuario = Usuario.builder()
        .username("edit@test.com")
        .password("hash")
        .nombre("Nombre Viejo")
        .role(Role.USUARIO)
        .localidad(localidadTest)
        .build();
    usuarioRepository.save(usuario);

    String token = jwtService.generateToken(Map.of("rol", "USUARIO"), usuario);

    // 1. Generamos dinámicamente una imagen PNG real de 1x1 píxel en memoria
    BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", baos);
    byte[] imageBytes = baos.toByteArray();

    // 2. Usamos los bytes de la imagen real para el MockMultipartFile
    MockMultipartFile fotoMock = new MockMultipartFile(
        "fotoPerfil", "test.png", "image/png", imageBytes
    );

    mockMvc.perform(multipart("/api/v1/auth/usuarios/perfil")
            .file(fotoMock)
            .param("nombre", "Nombre Nuevo")
            .param("biografia", "Bio actualizada")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .with(request -> {
              request.setMethod("PUT");
              return request;
            }))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre", is("Nombre Nuevo")))
        .andExpect(jsonPath("$.biografia", is("Bio actualizada")));
  }
}