package edu.ies63.rcc2026_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.ies63.rcc2026_backend.dto.EventoCrearRequestDTO;
import edu.ies63.rcc2026_backend.dto.EventoEditarRequestDTO;
import edu.ies63.rcc2026_backend.dto.RespuestaInvitacionDTO;
import edu.ies63.rcc2026_backend.enums.EstadoEvento;
import edu.ies63.rcc2026_backend.enums.Pais;
import edu.ies63.rcc2026_backend.enums.Provincia;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.enums.TipoImagen;
import edu.ies63.rcc2026_backend.model.Evento;
import edu.ies63.rcc2026_backend.model.GrupoEvento;
import edu.ies63.rcc2026_backend.model.ImagenPerfil;
import edu.ies63.rcc2026_backend.model.Localidad;
import edu.ies63.rcc2026_backend.model.Notificacion;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.EventoRepository;
import edu.ies63.rcc2026_backend.repository.GrupoEventoRepository;
import edu.ies63.rcc2026_backend.repository.LocalidadRepository;
import edu.ies63.rcc2026_backend.repository.NotificacionRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class EventoIntegrationTest {

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
    private EventoRepository eventoRepository;

    @Autowired
    private GrupoEventoRepository grupoEventoRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private Usuario organizador;
    private Usuario grupoArtista;
    private Localidad localidadTest;

    @BeforeEach
    void setUp() {
        // Limpieza en orden por dependencias de claves foráneas
        notificacionRepository.deleteAll();
        grupoEventoRepository.deleteAll();
        eventoRepository.deleteAll();
        usuarioRepository.deleteAll();
        localidadRepository.deleteAll();

        // 1. Crear Localidad con valores válidos
        localidadTest = new Localidad();
        localidadTest.setLocalidad("Las Toscas");
        localidadTest.setProvincia(Provincia.SANTA_FE);
        localidadTest.setPais(Pais.ARGENTINA);
        localidadTest = localidadRepository.save(localidadTest);

        // 2. Crear Organizador
        organizador = Usuario.builder()
                .username("organizador@rcc.com")
                .password("password123")
                .nombre("Centro Cultural")
                .role(Role.ORGANIZADOR)
                .localidad(localidadTest)
                .imagenesPerfil(new ArrayList<>())
                .redes(new ArrayList<>())
                .build();
        organizador = usuarioRepository.save(organizador);

        // 3. Crear Artista/Grupo con Foto de Perfil
        grupoArtista = Usuario.builder()
                .username("grupo@rcc.com")
                .password("password123")
                .nombre("Banda del Norte")
                .role(Role.GRUPO)
                .localidad(localidadTest)
                .imagenesPerfil(new ArrayList<>())
                .redes(new ArrayList<>())
                .build();
        grupoArtista = usuarioRepository.save(grupoArtista);

        ImagenPerfil fotoPerfil = ImagenPerfil.builder()
                .usuario(grupoArtista)
                .tipoImagen(TipoImagen.FOTOPERFIL)
                .url("https://rcc.com/uploads/banda.png")
                .build();
        grupoArtista.getImagenesPerfil().add(fotoPerfil);
        grupoArtista = usuarioRepository.save(grupoArtista);
    }

    @Test
    @DisplayName("Flujo Completo: Creación -> Invitación -> Transición a PUBLICADO -> Consulta Pública y Edición")
    void debeEjecutarFlujoCompletoDeEvento() throws Exception {
        LocalDate fechaEvento = LocalDate.now().plusMonths(1);
        LocalTime horaEvento = LocalTime.of(21, 30);

        String tokenOrg = jwtService.generateToken(Map.of("rol", "ORGANIZADOR"), organizador);
        String tokenGrupo = jwtService.generateToken(Map.of("rol", "GRUPO"), grupoArtista);

        // =========================================================================
        // 1. CREAR EVENTO (Organizador)
        // =========================================================================
        EventoCrearRequestDTO crearReq = new EventoCrearRequestDTO(
                "Gran Peña Folclórica",
                "Música en vivo y comidas típicas",
                fechaEvento,
                horaEvento,
                localidadTest.getId(),
                "Calle 10 N° 550",
                List.of(grupoArtista.getId())
        );

        MvcResult resultCrear = mockMvc.perform(post("/api/v1/eventos/crear")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOrg)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre", is("Gran Peña Folclórica")))
                .andExpect(jsonPath("$.estado", is("PENDIENTE")))
                .andReturn();

        Long eventoId = objectMapper.readTree(resultCrear.getResponse().getContentAsString()).get("id").asLong();

        List<Notificacion> notificaciones = notificacionRepository.findAll();
        assertThat(notificaciones).hasSize(1);
        assertThat(notificaciones.get(0).getPerfil().getId()).isEqualTo(grupoArtista.getId());

        // =========================================================================
        // 2. VERIFICACIÓN DE VISIBILIDAD (Estado PENDIENTE)
        // =========================================================================
        // Visitante anónimo no debe ver eventos en PENDIENTE en el calendario mensual
        mockMvc.perform(get("/api/v1/eventos")
                        .param("anio", String.valueOf(fechaEvento.getYear()))
                        .param("mes", String.valueOf(fechaEvento.getMonthValue())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        // =========================================================================
        // 3. RESPONDER INVITACIÓN (Artista Acepta)
        // =========================================================================
        GrupoEvento invitacion = grupoEventoRepository
                .findByEvento_IdAndArtista_Id(eventoId, grupoArtista.getId())
                .orElseThrow();

        RespuestaInvitacionDTO aceptarDto = new RespuestaInvitacionDTO(true);

        mockMvc.perform(patch("/api/v1/eventos/invitaciones/" + invitacion.getId() + "/responder")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGrupo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aceptarDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Participación confirmada exitosamente")));

        Evento eventoActualizado = eventoRepository.findById(eventoId).orElseThrow();
        assertThat(eventoActualizado.getEstado()).isEqualTo(EstadoEvento.PUBLICADO);

        // =========================================================================
        // 4. VERIFICACIÓN PÚBLICA (Estado PUBLICADO)
        // =========================================================================
        // Visitante anónimo ahora lo ve en /proximos
        mockMvc.perform(get("/api/v1/eventos/proximos")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(eventoId))
                .andExpect(jsonPath("$.content[0].nombre").value("Gran Peña Folclórica"));

        // Detalle del evento con foto de perfil del artista confirmada
        mockMvc.perform(get("/api/v1/eventos/" + eventoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.artistas[0].fotoPerfil").value("https://rcc.com/uploads/banda.png"))
                .andExpect(jsonPath("$.artistas[0].nombre").value("Banda del Norte"));

        // =========================================================================
        // 5. EDICIÓN PARCIAL (Organizador edita detalle)
        // =========================================================================
        EventoEditarRequestDTO editarReq = new EventoEditarRequestDTO(
                null,
                "Detalle con buffet y sorteos",
                null, null, null, null, null, null
        );

        mockMvc.perform(patch("/api/v1/eventos/editar/" + eventoId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOrg)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editarReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evento.detalle", is("Detalle con buffet y sorteos")));
    }

    @Test
    @DisplayName("Debe rechazar con 403 Forbidden si un usuario no organizador intenta editar el evento")
    void debeRetornarForbiddenSiUsuarioNoEsOrganizadorAlEditar() throws Exception {
        Evento evento = Evento.builder()
                .nombre("Encuentro Privado")
                .detalle("Detalle")
                .fecha(LocalDate.now().plusDays(10))
                .hora(LocalTime.of(20, 0))
                .localidad(localidadTest)
                .organizador(organizador)
                .estado(EstadoEvento.PUBLICADO)
                .build();
        evento = eventoRepository.save(evento);

        Usuario otroUsuario = Usuario.builder()
                .username("ajeno@rcc.com")
                .password("password123")
                .nombre("Usuario Ajeno")
                .role(Role.USUARIO)
                .localidad(localidadTest)
                .imagenesPerfil(new ArrayList<>())
                .redes(new ArrayList<>())
                .build();
        usuarioRepository.save(otroUsuario);

        String tokenAjeno = jwtService.generateToken(Map.of("rol", "USUARIO"), otroUsuario);

        EventoEditarRequestDTO req = new EventoEditarRequestDTO(
                "Nombre Hackeado", null, null, null, null, null, null, null
        );

        mockMvc.perform(patch("/api/v1/eventos/editar/" + evento.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAjeno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH toggle-activo cambia el estado del evento")
    void debeAlternarActivoEvento() throws Exception {
        Evento evento = Evento.builder()
            .nombre("Show")
            .detalle("Detalle del show") // <-- Requerido por la BD
            .fecha(LocalDate.now().plusDays(10)) // <-- Requerido
            .hora(LocalTime.of(20, 0)) // <-- Requerido
            .localidad(localidadTest) // <-- Requerido
            .estado(EstadoEvento.PUBLICADO)
            .organizador(organizador)
            .activo(true)
            .build();
        evento = eventoRepository.save(evento);

        String tokenOrg = jwtService.generateToken(Map.of("rol", "ORGANIZADOR"), organizador);

        mockMvc.perform(patch("/api/v1/eventos/" + evento.getId() + "/toggle-activo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOrg))
            .andExpect(status().isNoContent());

        Evento actualizado = eventoRepository.findById(evento.getId()).orElseThrow();
        assertThat(actualizado.getActivo()).isFalse();
    }

    @Test
    @DisplayName("GET mis-invitaciones retorna invitaciones para un grupo")
    void debeListarMisInvitaciones() throws Exception {
        String tokenGrupo = jwtService.generateToken(Map.of("rol", "GRUPO"), grupoArtista);
        mockMvc.perform(get("/api/v1/eventos/mis-invitaciones")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGrupo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}