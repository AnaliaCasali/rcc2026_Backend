package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.*;
import edu.ies63.rcc2026_backend.enums.*;
import edu.ies63.rcc2026_backend.exception.EventoConflictException;
import edu.ies63.rcc2026_backend.exception.EventoNoPermitidoException;
import edu.ies63.rcc2026_backend.model.*;
import edu.ies63.rcc2026_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceImplTest {

  @Mock
  private EventoRepository eventoRepository;
  @Mock
  private GrupoEventoRepository grupoEventoRepository;
  @Mock
  private UsuarioRepository usuarioRepository;
  @Mock
  private LocalidadRepository localidadRepository;
  @Mock
  private NotificacionRepository notificacionRepository;

  @InjectMocks
  private EventoServiceImpl eventoService;

  private Usuario organizador;
  private Usuario artistaGrupo;
  private Localidad localidad;

  @BeforeEach
  void setUp() {
    organizador = Usuario.builder()
        .id(1L)
        .username("organizador@mail.com")
        .nombre("Org Principal")
        .role(Role.ORGANIZADOR)
        .imagenesPerfil(new ArrayList<>())
        .build();

    ImagenPerfil fotoArtista = ImagenPerfil.builder()
        .id(10L)
        .url("https://bucket.s3/foto-perfil.jpg")
        .tipoImagen(TipoImagen.FOTOPERFIL)
        .build();

    artistaGrupo = Usuario.builder()
        .id(2L)
        .username("banda@mail.com")
        .nombre("Banda de Rock")
        .role(Role.GRUPO)
        .imagenesPerfil(List.of(fotoArtista))
        .build();

    localidad = new Localidad();
    localidad.setId(5L);
    localidad.setLocalidad("Reconquista");
    localidad.setProvincia(Provincia.SANTA_FE);
  }

  // =========================================================================
  // HU-09: CREAR Y EDITAR EVENTOS
  // =========================================================================
  @Nested
  @DisplayName("Tests HU-09: Crear y Editar Evento")
  class CrearYEditarEventoTests {

    @Test
    @DisplayName("crearEvento: Lanza EventoNoPermitidoException si el rol no es ORGANIZADOR ni GRUPO")
    void crearEvento_rolNoPermitido_lanzaEventoNoPermitidoException() {
      Usuario usuarioComun = Usuario.builder()
          .id(3L)
          .username("usuario@mail.com")
          .role(Role.USUARIO)
          .build();

      when(usuarioRepository.findByUsername("usuario@mail.com")).thenReturn(Optional.of(usuarioComun));

      EventoCrearRequestDTO req = new EventoCrearRequestDTO(
          "Festival", "Detalle", LocalDate.now().plusDays(10), LocalTime.of(20, 0),
          5L, "Calle 1", List.of(2L)
      );

      assertThatThrownBy(() -> eventoService.crearEvento(req, "usuario@mail.com"))
          .isInstanceOf(EventoNoPermitidoException.class)
          .hasMessageContaining("Solo los organizadores pueden crear eventos");
    }

    @Test
    @DisplayName("crearEvento: Éxito con estado PENDIENTE cuando el creador no se incluye en artistas")
    void crearEvento_exitoso_quedaPendienteYEnviaNotificaciones() {
      when(usuarioRepository.findByUsername(organizador.getUsername())).thenReturn(Optional.of(organizador));
      when(localidadRepository.findById(5L)).thenReturn(Optional.of(localidad));
      when(usuarioRepository.findAllById(anySet())).thenReturn(List.of(artistaGrupo));
      when(eventoRepository.save(any(Evento.class))).thenAnswer(inv -> {
        Evento e = inv.getArgument(0);
        e.setId(100L);
        return e;
      });

      EventoCrearRequestDTO req = new EventoCrearRequestDTO(
          "Festival Rock", "Detalle", LocalDate.now().plusDays(10), LocalTime.of(20, 0),
          5L, "Av Siempre Viva 123", List.of(2L)
      );

      EventoDetalleResponseDTO res = eventoService.crearEvento(req, organizador.getUsername());

      assertThat(res.id()).isEqualTo(100L);
      assertThat(res.estado()).isEqualTo(EstadoEvento.PENDIENTE);
      assertThat(res.artistas()).hasSize(1);
      assertThat(res.artistas().get(0).fotoPerfil()).isEqualTo("https://bucket.s3/foto-perfil.jpg");
      verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("editarEvento: Lanza EventoConflictException si intenta editar campos críticos a menos de 3 días")
    void editarEvento_fueraDeMargenCriticos_lanzaEventoConflictException() {
      LocalDate fechaEventoProxima = LocalDate.now().plusDays(2); // Menos de 3 días
      Evento eventoExistente = Evento.builder()
          .id(50L)
          .nombre("Evento Próximo")
          .fecha(fechaEventoProxima)
          .hora(LocalTime.of(21, 0))
          .organizador(organizador)
          .localidad(localidad)
          .grupos(new ArrayList<>())
          .build();

      when(eventoRepository.findById(50L)).thenReturn(Optional.of(eventoExistente));

      EventoEditarRequestDTO req = new EventoEditarRequestDTO(
          null, null, fechaEventoProxima.plusDays(1), null, null, null, null, null
      );

      assertThatThrownBy(() -> eventoService.editarEvento(50L, req, organizador.getUsername()))
          .isInstanceOf(EventoConflictException.class)
          // Buscamos substring para evitar errores de encoding con tildes
          .hasMessageContaining("no puede modificarse faltando 3 d");
    }

    @Test
    @DisplayName("editarEvento: Lanza ResponseStatusException si al agregar artistas supera el límite de 20")
    void editarEvento_superaLimite20Artistas_lanzaBadRequest() {
      Evento eventoExistente = Evento.builder()
          .id(50L)
          .nombre("Gran Encuentro")
          .fecha(LocalDate.now().plusDays(10))
          .hora(LocalTime.of(21, 0))
          .organizador(organizador)
          .localidad(localidad)
          .grupos(new ArrayList<>())
          .build();

      for (long i = 1; i <= 19; i++) {
        eventoExistente.getGrupos().add(GrupoEvento.builder().id(i).build());
      }

      when(eventoRepository.findById(50L)).thenReturn(Optional.of(eventoExistente));
      when(grupoEventoRepository.existsByEvento_IdAndArtista_Id(anyLong(), anyLong())).thenReturn(false);

      List<Long> nuevosArtistasIds = List.of(101L, 102L); // 19 + 2 = 21 > 20
      EventoEditarRequestDTO req = new EventoEditarRequestDTO(
          null, null, null, null, null, null, nuevosArtistasIds, null
      );

      assertThatThrownBy(() -> eventoService.editarEvento(50L, req, organizador.getUsername()))
          .isInstanceOf(ResponseStatusException.class)
          // Substring sin tilde en límite
          .hasMessageContaining("mite de 20 agrupaciones/artistas");
    }
  }

  // =========================================================================
  // HU-10: CALENDARIO, PRÓXIMOS Y DETALLE
  // =========================================================================
  @Nested
  @DisplayName("Tests HU-10: Listar y Detalle de Eventos")
  class ListarYDetalleEventosTests {

    @Test
    @DisplayName("obtenerDetalleEvento: Lanza 404 a visitante si el evento está en PENDIENTE")
    void obtenerDetalleEvento_eventoPendienteYVisitanteAnonimo_lanzaNotFound() {
      Evento eventoPendiente = Evento.builder()
          .id(70L)
          .estado(EstadoEvento.PENDIENTE)
          .organizador(organizador)
          .grupos(new ArrayList<>())
          .build();

      when(eventoRepository.findById(70L)).thenReturn(Optional.of(eventoPendiente));

      assertThatThrownBy(() -> eventoService.obtenerDetalleEvento(70L, null))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("El evento no existe");
    }
    // NUEVO: evento desactivado (toggle-activo)
    @Test
    @DisplayName("obtenerDetalleEvento: Lanza 404 a visitante si el evento está desactivado")
    void obtenerDetalleEvento_eventoDesactivadoYVisitante_lanzaNotFound() {
      Evento eventoDesactivado = Evento.builder()
          .id(71L)
          .estado(EstadoEvento.PUBLICADO)
          .activo(false)
          .organizador(organizador)
          .grupos(new ArrayList<>())
          .build();

      when(eventoRepository.findById(71L)).thenReturn(Optional.of(eventoDesactivado));

      assertThatThrownBy(() -> eventoService.obtenerDetalleEvento(71L, null))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("El evento no existe");
    }

    @Test
    @DisplayName("obtenerDetalleEvento: El organizador ve su evento desactivado con activo = false")
    void obtenerDetalleEvento_eventoDesactivadoYOrganizador_retornaActivoFalse() {
      Evento eventoDesactivado = Evento.builder()
          .id(72L)
          .nombre("Peña pausada")
          .estado(EstadoEvento.PUBLICADO)
          .activo(false)
          .organizador(organizador)
          .localidad(localidad)
          .fecha(LocalDate.now().plusDays(10))
          .hora(LocalTime.of(21, 0))
          .grupos(new ArrayList<>())
          .build();

      when(eventoRepository.findById(72L)).thenReturn(Optional.of(eventoDesactivado));

      EventoDetalleResponseDTO detalle = eventoService.obtenerDetalleEvento(72L, organizador.getUsername());

      assertThat(detalle.activo()).isFalse();
    }


    @Test
    @DisplayName("listarProximos: Resuelve seguidor y mapea correctamente a DTO paginado")
    void listarProximos_conSesionYSeguidos_deberiaDelegarEnRepository() {
      Pageable pageable = PageRequest.of(0, 10);
      when(usuarioRepository.findByUsername(organizador.getUsername())).thenReturn(Optional.of(organizador));

      Evento eventoPublicado = Evento.builder()
          .id(80L)
          .nombre("Show Rock")
          .fecha(LocalDate.now().plusDays(1))
          .hora(LocalTime.of(22, 0))
          .localidad(localidad)
          .organizador(organizador)
          .estado(EstadoEvento.PUBLICADO)
          .build();

      Page<Evento> paginaMock = new PageImpl<>(List.of(eventoPublicado));
      when(eventoRepository.findProximosParaUsuario(any(), any(), eq(EstadoEvento.PUBLICADO), eq(organizador.getUsername()), isNull(), eq(1L), eq(pageable)))
          .thenReturn(paginaMock);

      Page<EventoCalendarioDTO> resultado = eventoService.listarProximos(null, true, organizador.getUsername(), pageable);

      assertThat(resultado.getContent()).hasSize(1);
      assertThat(resultado.getContent().get(0).nombre()).isEqualTo("Show Rock");
      assertThat(resultado.getContent().get(0).organizadorNombre()).isEqualTo("Org Principal");
    }
  }

  // =========================================================================
  // HU-11: GESTIÓN DE INVITACIONES (ARTISTAS)
  // =========================================================================
  @Nested
  @DisplayName("Tests HU-11: Bandeja y Respuesta de Invitaciones")
  class InvitacionesTests {

    @Test
    @DisplayName("responderInvitacion: Aceptar pasa invitación a ACEPTADA y evento a PUBLICADO")
    void responderInvitacion_aceptar_publicaEventoYNotificaOrganizador() {
      Evento evento = Evento.builder()
          .id(90L)
          .nombre("Festival Folclórico")
          .estado(EstadoEvento.PENDIENTE)
          .organizador(organizador)
          .build();

      GrupoEvento invitacion = GrupoEvento.builder()
          .id(15L)
          .evento(evento)
          .artista(artistaGrupo)
          .estadoInvitacion(EstadoInvitacion.PENDIENTE)
          .confirmado(false)
          .build();

      when(grupoEventoRepository.findByIdAndArtista_Username(15L, artistaGrupo.getUsername()))
          .thenReturn(Optional.of(invitacion));

      String res = eventoService.responderInvitacion(15L, true, artistaGrupo.getUsername());

      assertThat(res).isEqualTo("Participación confirmada exitosamente");
      assertThat(invitacion.getEstadoInvitacion()).isEqualTo(EstadoInvitacion.ACEPTADA);
      assertThat(invitacion.getConfirmado()).isTrue();
      assertThat(evento.getEstado()).isEqualTo(EstadoEvento.PUBLICADO);
      verify(eventoRepository, times(1)).save(evento);
      verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("responderInvitacion: Rechazar pasa invitación a RECHAZADA sin publicar evento")
    void responderInvitacion_rechazar_cambiaEstadoYNotificaOrganizador() {
      Evento evento = Evento.builder()
          .id(90L)
          .nombre("Festival Folclórico")
          .estado(EstadoEvento.PENDIENTE)
          .organizador(organizador)
          .build();

      GrupoEvento invitacion = GrupoEvento.builder()
          .id(15L)
          .evento(evento)
          .artista(artistaGrupo)
          .estadoInvitacion(EstadoInvitacion.PENDIENTE)
          .confirmado(false)
          .build();

      when(grupoEventoRepository.findByIdAndArtista_Username(15L, artistaGrupo.getUsername()))
          .thenReturn(Optional.of(invitacion));

      String res = eventoService.responderInvitacion(15L, false, artistaGrupo.getUsername());

      assertThat(res).isEqualTo("Invitación al evento rechazada");
      assertThat(invitacion.getEstadoInvitacion()).isEqualTo(EstadoInvitacion.RECHAZADA);
      assertThat(invitacion.getConfirmado()).isFalse();
      assertThat(evento.getEstado()).isEqualTo(EstadoEvento.PENDIENTE);
      verify(eventoRepository, never()).save(evento);
      verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }
  }

  // =========================================================================
  // NUEVOS TESTS QUE FALTABAN O FALLABAN
  // =========================================================================

  @Test
  @DisplayName("crearEvento: Lanza ResponseStatusException si la fecha y hora son anteriores a hoy")
  void crearEvento_fechaPasada_lanzaBadRequest() {
    when(usuarioRepository.findByUsername(organizador.getUsername())).thenReturn(Optional.of(organizador));

    EventoCrearRequestDTO req = new EventoCrearRequestDTO(
        "Festival Pasado", "Detalle", LocalDate.now().minusDays(1), LocalTime.of(20, 0),
        5L, "Calle 1", List.of(2L)
    );

    assertThatThrownBy(() -> eventoService.crearEvento(req, organizador.getUsername()))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("posteriores a la actual");
  }

  @Test
  @DisplayName("crearEvento: Lanza ResponseStatusException si el invitado no es un grupo")
  void crearEvento_artistaNoEsGrupo_lanzaBadRequest() {
    when(usuarioRepository.findByUsername(organizador.getUsername())).thenReturn(Optional.of(organizador));
    when(localidadRepository.findById(5L)).thenReturn(Optional.of(localidad));

    // Simulamos un usuario invitado que no es de rol GRUPO
    Usuario artistaInvalido = Usuario.builder().id(2L).role(Role.USUARIO).build();
    when(usuarioRepository.findAllById(anySet())).thenReturn(List.of(artistaInvalido));

    EventoCrearRequestDTO req = new EventoCrearRequestDTO(
        "Festival", "Detalle", LocalDate.now().plusDays(10), LocalTime.of(20, 0),
        5L, "Calle 1", List.of(2L)
    );

    assertThatThrownBy(() -> eventoService.crearEvento(req, organizador.getUsername()))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Solo se puede invitar a grupos o artistas");
  }

  @Test
  @DisplayName("editarEvento: Lanza ResponseStatusException si el usuario no es el organizador")
  void editarEvento_usuarioNoEsOrganizador_lanzaForbidden() {
    Evento eventoExistente = Evento.builder().id(50L).organizador(organizador).build();
    when(eventoRepository.findById(50L)).thenReturn(Optional.of(eventoExistente));
    EventoEditarRequestDTO req = new EventoEditarRequestDTO("Nuevo Nombre", null, null, null, null, null, null, null);

    assertThatThrownBy(() -> eventoService.editarEvento(50L, req, "otrousuario@mail.com"))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("No tienes permiso para editar este evento");
  }

  @Test
  @DisplayName("editarEvento: Lanza EventoConflictException si el evento ya finalizó")
  void editarEvento_eventoFinalizado_lanzaConflict() {
    Evento eventoFinalizado = Evento.builder()
        .id(50L)
        .fecha(LocalDate.now().minusDays(2))
        .hora(LocalTime.of(20, 0))
        .organizador(organizador)
        .build();
    when(eventoRepository.findById(50L)).thenReturn(Optional.of(eventoFinalizado));
    EventoEditarRequestDTO req = new EventoEditarRequestDTO("Edicion Tardía", null, null, null, null, null, null, null);

    assertThatThrownBy(() -> eventoService.editarEvento(50L, req, organizador.getUsername()))
        .isInstanceOf(EventoConflictException.class)
        // String seguro sin tildes
        .hasMessageContaining("El evento ya finaliz");
  }

  @Test
  @DisplayName("tieneCambios: Retorna false si todos los campos son nulos o vacíos")
  void tieneCambios_TodoNulo_RetornaFalse() {
    EventoEditarRequestDTO dto = new EventoEditarRequestDTO(
        null, null, null, null, null, null, null, null
    );
    assertThat(dto.tieneCambios()).isFalse();
  }

  @Test
  @DisplayName("tieneCambios: Retorna true si al menos un campo tiene datos")
  void tieneCambios_ConDatos_RetornaTrue() {
    EventoEditarRequestDTO dtoNombre = new EventoEditarRequestDTO(
        "Nuevo Nombre", null, null, null, null, null, null, null
    );
    assertThat(dtoNombre.tieneCambios()).isTrue();

    EventoEditarRequestDTO dtoListas = new EventoEditarRequestDTO(
        null, null, null, null, null, null, List.of(1L), null
    );
    assertThat(dtoListas.tieneCambios()).isTrue();
  }

  @Test
  @DisplayName("crearEvento: Lanza BadRequest si la localidad no existe")
  void crearEvento_localidadInexistente_lanzaBadRequest() {
    when(usuarioRepository.findByUsername(organizador.getUsername())).thenReturn(Optional.of(organizador));
    when(localidadRepository.findById(999L)).thenReturn(Optional.empty());

    EventoCrearRequestDTO req = new EventoCrearRequestDTO(
        "Festival", "Detalle", LocalDate.now().plusDays(10), LocalTime.of(20, 0),
        999L, "Calle 1", List.of(2L)
    );

    assertThatThrownBy(() -> eventoService.crearEvento(req, organizador.getUsername()))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("La localidad no existe");
  }

  @Test
  @DisplayName("crearEvento: Lanza BadRequest si se envían artistas duplicados")
  void crearEvento_artistasRepetidos_lanzaBadRequest() {
    when(usuarioRepository.findByUsername(organizador.getUsername())).thenReturn(Optional.of(organizador));
    when(localidadRepository.findById(5L)).thenReturn(Optional.of(localidad));

    EventoCrearRequestDTO req = new EventoCrearRequestDTO(
        "Festival", "Detalle", LocalDate.now().plusDays(10), LocalTime.of(20, 0),
        5L, "Calle 1", List.of(2L, 2L)
    );

    assertThatThrownBy(() -> eventoService.crearEvento(req, organizador.getUsername()))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("No se permiten artistas repetidos");
  }

  @Test
  @DisplayName("editarEvento: Lanza BadRequest si el artista a quitar no pertenece al evento")
  void editarEvento_quitarArtistaAjenjo_lanzaBadRequest() {
    Evento eventoExistente = Evento.builder()
        .id(50L).fecha(LocalDate.now().plusDays(10)).hora(LocalTime.of(21, 0))
        .organizador(organizador).localidad(localidad).grupos(new ArrayList<>()).build();
    when(eventoRepository.findById(50L)).thenReturn(Optional.of(eventoExistente));
    when(grupoEventoRepository.findByEvento_IdAndArtista_Id(50L, 99L)).thenReturn(Optional.empty());

    EventoEditarRequestDTO req = new EventoEditarRequestDTO(
        null, null, null, null, null, null, null, List.of(99L)
    );

    assertThatThrownBy(() -> eventoService.editarEvento(50L, req, organizador.getUsername()))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("El artista no pertenece al evento");
  }

  @Test
  @DisplayName("editarEvento: Lanza BadRequest si al quitar artistas la lista queda vacía")
  void editarEvento_listaArtistasQuedaVacia_lanzaBadRequest() {
    Evento eventoExistente = Evento.builder()
        .id(50L).fecha(LocalDate.now().plusDays(10)).hora(LocalTime.of(21, 0))
        .organizador(organizador).localidad(localidad).grupos(new ArrayList<>()).build();

    GrupoEvento gePendiente = GrupoEvento.builder().estadoInvitacion(EstadoInvitacion.PENDIENTE).build();
    eventoExistente.getGrupos().add(gePendiente);

    when(eventoRepository.findById(50L)).thenReturn(Optional.of(eventoExistente));
    when(grupoEventoRepository.findByEvento_IdAndArtista_Id(50L, 2L)).thenReturn(Optional.of(gePendiente));

    EventoEditarRequestDTO req = new EventoEditarRequestDTO(
        null, null, null, null, null, null, null, List.of(2L)
    );

    assertThatThrownBy(() -> eventoService.editarEvento(50L, req, organizador.getUsername()))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("El evento debe conservar al menos 1 artista");
  }

  // =========================================================================
  // METODOS CORREGIDOS QUE EVITAN ERRORES DE NULLPOINTER Y ENCODING
  // =========================================================================

  @Test
  @DisplayName("obtenerDetalleEvento: Retorna el evento si el usuario actual es un artista invitado (PENDIENTE)")
  void obtenerDetalleEvento_eventoPendienteYEsInvitado_retornaEvento() {
    // CORRECCION 1: Agregamos fecha y hora al mock para que "LocalDateTime.of" no tire NPE
    Evento eventoPendiente = Evento.builder()
        .id(70L)
        .nombre("Evento Privado")
        .estado(EstadoEvento.PENDIENTE)
        .organizador(organizador)
        .localidad(localidad)
        .fecha(LocalDate.now().plusDays(5))
        .hora(LocalTime.of(20, 0))
        .grupos(new ArrayList<>())
        .build();

    GrupoEvento invitacion = GrupoEvento.builder()
        .artista(artistaGrupo)
        .estadoInvitacion(EstadoInvitacion.PENDIENTE)
        .build();
    eventoPendiente.getGrupos().add(invitacion);

    when(eventoRepository.findById(70L)).thenReturn(Optional.of(eventoPendiente));

    EventoDetalleResponseDTO detalle = eventoService.obtenerDetalleEvento(70L, artistaGrupo.getUsername());

    assertThat(detalle.id()).isEqualTo(70L);
    assertThat(detalle.estado()).isEqualTo(EstadoEvento.PENDIENTE);
  }

  @Test
  @DisplayName("crearEvento: Lanza BadRequest si uno de los artistas no existe en DB")
  void crearEvento_artistaNoExisteEnDb_lanzaBadRequest() {
    when(usuarioRepository.findByUsername(organizador.getUsername())).thenReturn(Optional.of(organizador));
    when(localidadRepository.findById(5L)).thenReturn(Optional.of(localidad));
    when(usuarioRepository.findAllById(anySet())).thenReturn(List.of());

    EventoCrearRequestDTO req = new EventoCrearRequestDTO(
        "Festival", "Detalle", LocalDate.now().plusDays(10), LocalTime.of(20, 0),
        5L, "Calle 1", List.of(2L)
    );

    assertThatThrownBy(() -> eventoService.crearEvento(req, organizador.getUsername()))
        .isInstanceOf(ResponseStatusException.class)
        // CORRECCION 2: Substring sin tilde
        .hasMessageContaining("artistas seleccionados no existen");
  }

  @Test
  @DisplayName("editarEvento: Lanza BadRequest si no hay cambios y la lista agregar está vacía")
  void editarEvento_sinCambios_lanzaBadRequest() {
    EventoEditarRequestDTO req = new EventoEditarRequestDTO(
        null, null, null, null, null, null, List.of(), null
    );

    assertThatThrownBy(() -> eventoService.editarEvento(50L, req, organizador.getUsername()))
        .isInstanceOf(ResponseStatusException.class)
        // CORRECCION 2: Substring sin tilde
        .hasMessageContaining("Debes seleccionar una agrupaci");
  }

  @Test
  @DisplayName("editarEvento: Lanza EventoConflictException si se intenta quitar un artista ACEPTADO")
  void editarEvento_quitarArtistaConfirmado_lanzaConflict() {
    Evento eventoExistente = Evento.builder()
        .id(50L).fecha(LocalDate.now().plusDays(10)).hora(LocalTime.of(21, 0))
        .organizador(organizador).localidad(localidad).grupos(new ArrayList<>()).build();

    GrupoEvento geAceptado = GrupoEvento.builder().estadoInvitacion(EstadoInvitacion.ACEPTADA).build();
    when(eventoRepository.findById(50L)).thenReturn(Optional.of(eventoExistente));
    when(grupoEventoRepository.findByEvento_IdAndArtista_Id(50L, 2L)).thenReturn(Optional.of(geAceptado));

    EventoEditarRequestDTO req = new EventoEditarRequestDTO(
        null, null, null, null, null, null, null, List.of(2L)
    );

    assertThatThrownBy(() -> eventoService.editarEvento(50L, req, organizador.getUsername()))
        .isInstanceOf(EventoConflictException.class)
        // CORRECCION 2: Substring sin tilde
        .hasMessageContaining("No se puede quitar a un artista que ya confirm");
  }

  @Test
  @DisplayName("listarEventosCalendario: Lanza BadRequest si el mes es inválido")
  void listarEventosCalendario_mesInvalido_lanzaBadRequest() {
    assertThatThrownBy(() -> eventoService.listarEventosCalendario(2026, 13, null, false, null))
        .isInstanceOf(ResponseStatusException.class)
        // CORRECCION 2: Substring sin tilde
        .hasMessageContaining("Mes");
  }

  @Test
  @DisplayName("responderInvitacion: Lanza NotFound si la invitación no existe")
  void responderInvitacion_invitacionInexistente_lanzaNotFound() {
    when(grupoEventoRepository.findByIdAndArtista_Username(99L, artistaGrupo.getUsername()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> eventoService.responderInvitacion(99L, true, artistaGrupo.getUsername()))
        .isInstanceOf(ResponseStatusException.class)
        // CORRECCION 2: Substring sin tilde
        .hasMessageContaining("no encontrada");
  }

  @Nested
  @DisplayName("Tests HU-XX: Alternar Estado Activo")
  class AlternarActivoTests {
    @Test
    @DisplayName("alternarActivo: Cambia exitosamente el estado activo del evento")
    void alternarActivo_exitoso() {
      Evento evento = Evento.builder().id(10L).organizador(organizador).activo(true).build();
      when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

      eventoService.alternarActivo(10L, organizador.getUsername());

      assertThat(evento.getActivo()).isFalse();
      verify(eventoRepository, times(1)).save(evento);
    }

    @Test
    @DisplayName("alternarActivo: Lanza ResponseStatusException si el evento no existe")
    void alternarActivo_eventoNoExiste() {
      when(eventoRepository.findById(10L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> eventoService.alternarActivo(10L, organizador.getUsername()))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("El evento no existe");
    }

    @Test
    @DisplayName("alternarActivo: Lanza ResponseStatusException si el usuario no es el organizador")
    void alternarActivo_noEsOrganizador() {
      Evento evento = Evento.builder().id(10L).organizador(organizador).activo(true).build();
      when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

      assertThatThrownBy(() -> eventoService.alternarActivo(10L, "intruso@mail.com"))
          .isInstanceOf(ResponseStatusException.class)
          .hasMessageContaining("No tienes permiso");
    }
  }
}