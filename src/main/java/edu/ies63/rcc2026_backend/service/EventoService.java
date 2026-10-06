package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.*;
import java.util.List;

public interface EventoService {
    EventoDetalleResponseDTO crearEvento(EventoCrearRequestDTO request, String usernameOrganizador);
    EventoEdicionResponseDTO editarEvento(Long id, EventoEditarRequestDTO request, String usernameOrganizador);
    List<EventoCalendarioDTO> listarEventosCalendario(int anio, int mes, String usernameActual);
    EventoDetalleResponseDTO obtenerDetalleEvento(Long id, String usernameActual);
    List<InvitacionDetalleDTO> listarMisInvitaciones(String usernameArtista);
    String responderInvitacion(Long invitacionId, boolean aceptar, String usernameArtista);
}