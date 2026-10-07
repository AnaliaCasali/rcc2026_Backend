package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventoService {

    EventoDetalleResponseDTO crearEvento(EventoCrearRequestDTO request, String username);

    EventoEdicionResponseDTO editarEvento(Long id, EventoEditarRequestDTO request, String username);

    List<EventoCalendarioDTO> listarEventosCalendario(int anio, int mes, Long usuarioId, boolean soloSeguidos, String usernameActual);

    Page<EventoCalendarioDTO> listarProximos(Long usuarioId, boolean soloSeguidos, String usernameActual, Pageable pageable);

    EventoDetalleResponseDTO obtenerDetalleEvento(Long id, String usernameActual);

    List<InvitacionDetalleDTO> listarMisInvitaciones(String usernameArtista);

    String responderInvitacion(Long invitacionId, boolean aceptar, String usernameArtista);
}