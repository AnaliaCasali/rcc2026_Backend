package edu.ies63.rcc2026_backend.integration;

import edu.ies63.rcc2026_backend.enums.Pais;
import edu.ies63.rcc2026_backend.enums.Provincia;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Localidad;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class BusquedaIntegrationTest {

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

        // 1. Localidades de prueba (cercana y lejana)
        Localidad lasToscas = new Localidad();
        lasToscas.setLocalidad("Las Toscas");
        lasToscas.setProvincia(Provincia.SANTA_FE);
        lasToscas.setPais(Pais.ARGENTINA);
        lasToscas.setLatitud(-28.35);
        lasToscas.setLongitud(-59.26);
        lasToscas = localidadRepository.save(lasToscas);

        Localidad rosario = new Localidad();
        rosario.setLocalidad("Rosario");
        rosario.setProvincia(Provincia.SANTA_FE);
        rosario.setPais(Pais.ARGENTINA);
        rosario.setLatitud(-32.95);
        rosario.setLongitud(-60.64);
        rosario = localidadRepository.save(rosario);

        // 2. Usuarios con distintos roles y estados
        Usuario grupo = Usuario.builder()
                .username("danza@test.com")
                .password("hash")
                .nombre("Ballet Folklórico Auka")
                .role(Role.GRUPO)
                .biografia("Danzas tradicionales")
                .localidad(lasToscas)
                .debaja(false)
                .verificado(true)
                .build();
        usuarioRepository.save(grupo);

        Usuario organizador = Usuario.builder()
                .username("organizador@test.com")
                .password("hash")
                .nombre("Festival Las Toscas")
                .role(Role.ORGANIZADOR)
                .biografia("Organización de eventos")
                .localidad(lasToscas)
                .debaja(false)
                .verificado(true)
                .build();
        usuarioRepository.save(organizador);

        Usuario usuarioComun = Usuario.builder()
                .username("comun@test.com")
                .password("hash")
                .nombre("Espectador Auka")
                .role(Role.USUARIO) // Este nunca debe aparecer en los resultados
                .localidad(lasToscas)
                .debaja(false)
                .verificado(true)
                .build();
        usuarioRepository.save(usuarioComun);

        // Token para autenticar las peticiones
        token = jwtService.generateToken(Map.of("rol", "USUARIO"), usuarioComun);
    }

    @Test
    @DisplayName("1. Búsqueda por texto: filtra por coincidencia parcial y omite rol USUARIO")
    void buscarPorTexto_Exitoso() throws Exception {
        mockMvc.perform(get("/api/v1/busqueda")
                        .param("texto", "auka")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Ballet Folklórico Auka")))
                .andExpect(jsonPath("$[0].rol", is("GRUPO")));
    }

    @Test
    @DisplayName("2. Búsqueda por rol: si se solicita GRUPO devuelve únicamente agrupaciones")
    void buscarPorRol_FiltraSoloGrupos() throws Exception {
        mockMvc.perform(get("/api/v1/busqueda")
                        .param("rol", "GRUPO")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rol", is("GRUPO")));
    }

    @Test
    @DisplayName("3. Búsqueda por ubicación: filtra por nombre de localidad")
    void buscarPorUbicacion_Exitoso() throws Exception {
        mockMvc.perform(get("/api/v1/busqueda")
                        .param("ubicacion", "Las Toscas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("4. Búsqueda por GPS: calcula distancia y respeta el radio en km")
    void buscarPorGps_CalculaDistanciaCorrectamente() throws Exception {
        // Coordenadas coincidentes con Las Toscas
        mockMvc.perform(get("/api/v1/busqueda")
                        .param("lat", "-28.35")
                        .param("lng", "-59.26")
                        .param("radioKm", "50")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].distanciaKm", notNullValue()));
    }

    @Test
    @DisplayName("5. Búsqueda sin coincidencias: responde lista vacía [] con código 200")
    void buscarSinCoincidencias_RetornaVacioCon200() throws Exception {
        mockMvc.perform(get("/api/v1/busqueda")
                        .param("ubicacion", "Localidad Fantasma")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("6. Parámetros GPS incompletos: lanza BusquedaInvalidaException y devuelve 400")
    void buscarGpsIncompleto_Retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/busqueda")
                        .param("lat", "-28.35")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("lat y lng deben enviarse juntos")));
    }
}