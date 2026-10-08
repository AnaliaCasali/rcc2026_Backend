package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.*;
import edu.ies63.rcc2026_backend.enums.EstadoEvento;
import edu.ies63.rcc2026_backend.enums.EstadoInvitacion;
import edu.ies63.rcc2026_backend.service.EventoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoControllerTest {

    @Mock
    private EventoService eventoService;

    @Mock
    private Principal principal;

    @InjectMocks
    private EventoController eventoController;

    @Test
    @DisplayName("crearEvento: Llama al servicio y retorna 201 CREATED con el detalle")
    void crearEvento_ParametrosValidos_Retorna201() {
        EventoCrearRequestDTO requestDTO = new EventoCrearRequestDTO(
                "Gran Festival",
                "Detalle del evento",
                LocalDate.of(2026, 11, 20),
                LocalTime.of(20, 0),
                1L,
                "Av San Martín 500",
                List.of(2L)
        );

        EventoDetalleResponseDTO mockResponse = new EventoDetalleResponseDTO(
                10L,
                "Gran Festival",
                "Detalle del evento",
                LocalDate.of(2026, 11, 20),
                LocalTime.of(20, 0),
                new LocalidadSimpleDTO(1L, "Reconquista"),
                "Av San Martín 500",
                EstadoEvento.PENDIENTE,
                new OrganizadorSimpleDTO(1L, "Organizador", null),
                List.of(),
                new PermisosEventoDTO(true, true, true, LocalDate.of(2026, 11, 17))
        );

        when(principal.getName()).thenReturn("organizador@mail.com");
        when(eventoService.crearEvento(requestDTO, "organizador@mail.com")).thenReturn(mockResponse);

        ResponseEntity<EventoDetalleResponseDTO> response = eventoController.crearEvento(requestDTO, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(eventoService).crearEvento(requestDTO, "organizador@mail.com");
    }

    @Test
    @DisplayName("editarEvento: Llama al servicio y retorna 200 OK con el resumen de edición")
    void editarEvento_ParametrosValidos_Retorna200() {
        EventoEditarRequestDTO requestDTO = new EventoEditarRequestDTO(
                "Nuevo Nombre", null, null, null, null, null, null, null
        );

        EventoDetalleResponseDTO detalleMock = new EventoDetalleResponseDTO(
                10L, "Nuevo Nombre", "Detalle", LocalDate.of(2026, 11, 20), LocalTime.of(20, 0),
                new LocalidadSimpleDTO(1L, "Reconquista"), "Av San Martín 500", EstadoEvento.PUBLICADO,
                new OrganizadorSimpleDTO(1L, "Organizador", null), List.of(), null
        );
        EventoEdicionResponseDTO mockResponse = new EventoEdicionResponseDTO(
                detalleMock,
                new EventoEdicionResponseDTO.NotificacionEdicionResumen(false, 0, List.of())
        );

        when(principal.getName()).thenReturn("organizador@mail.com");
        when(eventoService.editarEvento(10L, requestDTO, "organizador@mail.com")).thenReturn(mockResponse);

        ResponseEntity<EventoEdicionResponseDTO> response = eventoController.editarEvento(10L, requestDTO, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(eventoService).editarEvento(10L, requestDTO, "organizador@mail.com");
    }

    @Test
    @DisplayName("listarEventosMes: Llama al servicio con o sin usuario autenticado y retorna 200 OK")
    void listarEventosMes_ParametrosValidos_Retorna200() {
        EventoCalendarioDTO item = new EventoCalendarioDTO(
                10L, "Festival Folclórico", LocalDate.of(2026, 11, 25), LocalTime.of(21, 0),
                "Reconquista", EstadoEvento.PUBLICADO, 1L, "Organizador"
        );

        when(eventoService.listarEventosCalendario(2026, 11, 5L, false, null))
                .thenReturn(List.of(item));

        ResponseEntity<List<EventoCalendarioDTO>> response = eventoController.listarEventosMes(
                2026, 11, 5L, false, null
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(item);
        verify(eventoService).listarEventosCalendario(2026, 11, 5L, false, null);
    }

    @Test
    @DisplayName("listarProximos: Llama al servicio con paginación y retorna 200 OK")
    void listarProximos_ParametrosValidos_Retorna200() {
        Pageable pageable = PageRequest.of(0, 10);
        EventoCalendarioDTO item = new EventoCalendarioDTO(
                11L, "Rock en la Plaza", LocalDate.of(2026, 12, 1), LocalTime.of(19, 0),
                "Avellaneda", EstadoEvento.PUBLICADO, 2L, "Municipalidad"
        );
        Page<EventoCalendarioDTO> paginaMock = new PageImpl<>(List.of(item));

        when(principal.getName()).thenReturn("usuario@mail.com");
        when(eventoService.listarProximos(null, true, "usuario@mail.com", pageable))
                .thenReturn(paginaMock);

        ResponseEntity<Page<EventoCalendarioDTO>> response = eventoController.listarProximos(
                null, true, pageable, principal
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(paginaMock);
        verify(eventoService).listarProximos(null, true, "usuario@mail.com", pageable);
    }

    @Test
    @DisplayName("obtenerPorId: Llama al servicio y retorna 200 OK con el detalle del evento")
    void obtenerPorId_EventoExiste_Retorna200() {
        EventoDetalleResponseDTO mockResponse = new EventoDetalleResponseDTO(
                10L, "Gran Festival", "Detalle", LocalDate.of(2026, 11, 20), LocalTime.of(20, 0),
                new LocalidadSimpleDTO(1L, "Reconquista"), "Av San Martín 500", EstadoEvento.PUBLICADO,
                new OrganizadorSimpleDTO(1L, "Organizador", null), List.of(), null
        );

        when(eventoService.obtenerDetalleEvento(10L, null)).thenReturn(mockResponse);

        ResponseEntity<EventoDetalleResponseDTO> response = eventoController.obtenerPorId(10L, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockResponse);
        verify(eventoService).obtenerDetalleEvento(10L, null);
    }


    @Test
    @DisplayName("listarMisInvitaciones: Llama al servicio con usuario logueado y retorna 200 OK")
    void listarMisInvitaciones_UsuarioAutenticado_Retorna200() {
        InvitacionDetalleDTO invitacionDTO = new InvitacionDetalleDTO(
                1L, 10L, "Gran Festival", "Detalle", LocalDate.of(2026, 11, 20), LocalTime.of(20, 0),
                "Av San Martín 500", "Reconquista, Santa Fe", "Organizador", EstadoInvitacion.PENDIENTE
        );

        when(principal.getName()).thenReturn("artista@mail.com");
        when(eventoService.listarMisInvitaciones("artista@mail.com")).thenReturn(List.of(invitacionDTO));

        ResponseEntity<List<InvitacionDetalleDTO>> response = eventoController.listarMisInvitaciones(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(invitacionDTO);
        verify(eventoService).listarMisInvitaciones("artista@mail.com");
    }

    @Test
    @DisplayName("responderInvitacion: Llama al servicio y retorna 200 OK con el mensaje de resultado")
    void responderInvitacion_AceptarInvitacion_Retorna200() {
        RespuestaInvitacionDTO requestDTO = new RespuestaInvitacionDTO(true);

        when(principal.getName()).thenReturn("artista@mail.com");
        when(eventoService.responderInvitacion(1L, true, "artista@mail.com"))
                .thenReturn("Participación confirmada exitosamente");

        ResponseEntity<Map<String, String>> response = eventoController.responderInvitacion(1L, requestDTO, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "Participación confirmada exitosamente");
        verify(eventoService).responderInvitacion(1L, true, "artista@mail.com");
    }
}