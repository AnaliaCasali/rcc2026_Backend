package edu.ies63.rcc2026_backend.integration;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class LocalidadIntegrationTest {
  @Container
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
      .withDatabaseName("rcc_test")
      .withUsername("test").withPassword("test");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", mysql::getJdbcUrl);
    registry.add("spring.datasource.username", mysql::getUsername);
    registry.add("spring.datasource.password", mysql::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private LocalidadRepository localidadRepository;
  @Autowired private UsuarioRepository usuarioRepository;
  @Autowired private JwtService jwtService;

  private String token;

  @BeforeEach
  void setUp() {
    Localidad loc = new Localidad();
    loc.setLocalidad("Resistencia");
    loc.setProvincia(Provincia.CHACO);
    loc.setPais(Pais.ARGENTINA);
    loc.setLatitud(-27.45);
    loc.setLongitud(-58.98);
    localidadRepository.save(loc);

    Usuario user = Usuario.builder()
        .username("loc@test.com")
        .password("hash")
        .nombre("Loc User")
        .role(Role.USUARIO)
        .debaja(false)
        .verificado(true)
        .build();
    usuarioRepository.save(user);
    token = jwtService.generateToken(Map.of("rol", "USUARIO"), user);
  }

  @Test
  void listarLocalidades_Exitoso() throws Exception {
    mockMvc.perform(get("/api/v1/localidades"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(337)))
        .andExpect(jsonPath("$[0].localidad", is("Aarón Castellanos")));
  }

  @Test
  void listarMapa_Exitoso() throws Exception {
    // Se requiere el token porque esta ruta no está explícitamente en el permitAll
    mockMvc.perform(get("/api/v1/localidades/mapa")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(337)))
        .andExpect(jsonPath("$[0].latitud", is(-27.4514)));
  }
}