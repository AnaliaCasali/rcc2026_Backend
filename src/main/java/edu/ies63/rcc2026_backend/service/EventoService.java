package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.EventoCreateDTO;
import edu.ies63.rcc2026_backend.dto.EventoResponseDTO;
import edu.ies63.rcc2026_backend.dto.EventoUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventoService {
    EventoResponseDTO crearEvento(EventoCreateDTO dto, String usernameOrganizador);
    EventoResponseDTO confirmarParticipacion(Long eventoId, String usernameArtista);
    Page<EventoResponseDTO> listarEventosPublicados(Pageable pageable);
    EventoResponseDTO obtenerPorId(Long id);
    EventoResponseDTO editarEvento(Long id, EventoUpdateDTO dto, String usernameOrganizador);
    List<EventoResponseDTO> listarEventosPorMesYAno(int anio, int mes);
}