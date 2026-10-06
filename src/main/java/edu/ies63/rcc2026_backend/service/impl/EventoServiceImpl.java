package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.ArtistaInvitadoDTO;
import edu.ies63.rcc2026_backend.dto.EventoCreateDTO;
import edu.ies63.rcc2026_backend.dto.EventoResponseDTO;
import edu.ies63.rcc2026_backend.dto.EventoUpdateDTO;
import edu.ies63.rcc2026_backend.enums.EstadoEvento;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.exception.EventoNoPermitidoException;
import edu.ies63.rcc2026_backend.exception.UsuarioNoEncontradoException;
import edu.ies63.rcc2026_backend.model.Evento;
import edu.ies63.rcc2026_backend.model.GrupoEvento;
import edu.ies63.rcc2026_backend.model.Localidad;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.EventoRepository;
import edu.ies63.rcc2026_backend.repository.GrupoEventoRepository;
import edu.ies63.rcc2026_backend.repository.LocalidadRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;
    private final GrupoEventoRepository grupoEventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LocalidadRepository localidadRepository;

    @Override
    @Transactional
    public EventoResponseDTO crearEvento(EventoCreateDTO dto, String usernameOrganizador) {
        Usuario organizador = usuarioRepository.findByUsername(usernameOrganizador)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Organizador no encontrado: " + usernameOrganizador));

        // Criterio: El organizador debe tener el rol adecuado
        if (organizador.getRole() != Role.ORGANIZADOR) {
            throw new EventoNoPermitidoException("El usuario debe tener rol ORGANIZADOR para crear eventos");
        }

        Localidad localidad = localidadRepository.findById(dto.localidadId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Localidad no encontrada"));

        // Validar lista de artistas invitados
        List<Usuario> artistas = usuarioRepository.findAllById(dto.artistasIds());
        if (artistas.isEmpty() || artistas.size() != dto.artistasIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno o más artistas seleccionados no son válidos");
        }

        Evento evento = Evento.builder()
                .organizador(organizador)
                .nombre(dto.nombre())
                .descripcion(dto.descripcion())
                .fechaEvento(dto.fechaEvento())
                .horaEvento(dto.horaEvento())
                .ubicacion(dto.ubicacion())
                .localidad(localidad)
                .estado(EstadoEvento.PENDIENTE) // Estado inicial requerido
                .grupos(new ArrayList<>())
                .build();

        for (Usuario artista : artistas) {
            GrupoEvento grupoEvento = GrupoEvento.builder()
                    .evento(evento)
                    .artista(artista)
                    .confirmado(false)
                    .build();
            evento.getGrupos().add(grupoEvento);
        }

        Evento eventoGuardado = eventoRepository.save(evento);
        return mapearADTO(eventoGuardado);
    }

    @Override
    @Transactional
    public EventoResponseDTO confirmarParticipacion(Long eventoId, String usernameArtista) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        GrupoEvento invitacion = grupoEventoRepository.findByEvento_IdAndArtista_Username(eventoId, usernameArtista)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no tiene una invitación para este evento"));

        invitacion.setConfirmado(true);
        grupoEventoRepository.save(invitacion);

        // Regla clave: Si al menos un artista confirma, pasa a estado PUBLICADO
        if (evento.getEstado() == EstadoEvento.PENDIENTE) {
            evento.setEstado(EstadoEvento.PUBLICADO);
            evento = eventoRepository.save(evento);
        }

        return mapearADTO(evento);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventoResponseDTO> listarEventosPublicados(Pageable pageable) {
        return eventoRepository.findByEstado(EstadoEvento.PUBLICADO, pageable)
                .map(this::mapearADTO);
    }

    @Override
    @Transactional(readOnly = true)
    public EventoResponseDTO obtenerPorId(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado con ID: " + id));
        return mapearADTO(evento);
    }

    private EventoResponseDTO mapearADTO(Evento e) {
        List<ArtistaInvitadoDTO> artistas = e.getGrupos().stream()
                .map(g -> new ArtistaInvitadoDTO(
                        g.getArtista().getId(),
                        g.getArtista().getNombre(),
                        g.getArtista().getUsername(),
                        g.getConfirmado()
                ))
                .toList();

        return new EventoResponseDTO(
                e.getId(),
                e.getNombre(),
                e.getDescripcion(),
                e.getFechaEvento(),
                e.getHoraEvento(),
                e.getUbicacion(),
                e.getLocalidad().getLocalidad() + ", " + e.getLocalidad().getProvincia(),
                e.getOrganizador().getNombre(),
                e.getOrganizador().getId(),
                e.getEstado(),
                e.getFechaPublic(),
                artistas
        );
    }

    @Override
    @Transactional
    public EventoResponseDTO editarEvento(Long id, EventoUpdateDTO dto, String usernameOrganizador) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado con ID: " + id));

        // Validar que el usuario que intenta editar sea el dueño del evento
        if (!evento.getOrganizador().getUsername().equals(usernameOrganizador)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar este evento");
        }

        // Validar existencia de la nueva localidad si cambió
        if (!evento.getLocalidad().getId().equals(dto.localidadId())) {
            Localidad nuevaLocalidad = localidadRepository.findById(dto.localidadId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Localidad no encontrada"));
            evento.setLocalidad(nuevaLocalidad);
        }

        evento.setNombre(dto.nombre());
        evento.setDescripcion(dto.descripcion());
        evento.setFechaEvento(dto.fechaEvento());
        evento.setHoraEvento(dto.horaEvento());
        evento.setUbicacion(dto.ubicacion());

        Evento guardado = eventoRepository.save(evento);
        return mapearADTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoResponseDTO> listarEventosPorMesYAno(int anio, int mes) {
        YearMonth yearMonth = YearMonth.of(anio, mes);
        LocalDate inicioMes = yearMonth.atDay(1);
        LocalDate finMes = yearMonth.atEndOfMonth();

        List<Evento> eventos = eventoRepository.findByEstadoAndFechaEventoBetween(
                EstadoEvento.PUBLICADO,
                inicioMes,
                finMes
        );

        return eventos.stream()
                .map(this::mapearADTOConArtistasConfirmados)
                .toList();
    }

    private EventoResponseDTO mapearADTOConArtistasConfirmados(Evento e) {
        // Filtrar solo los artistas confirmados para la visualización del usuario
        List<ArtistaInvitadoDTO> artistasConfirmados = e.getGrupos().stream()
                .filter(GrupoEvento::getConfirmado)
                .map(g -> new ArtistaInvitadoDTO(
                        g.getArtista().getId(),
                        g.getArtista().getNombre(),
                        g.getArtista().getUsername(),
                        g.getConfirmado()
                ))
                .toList();

        return new EventoResponseDTO(
                e.getId(),
                e.getNombre(),
                e.getDescripcion(),
                e.getFechaEvento(),
                e.getHoraEvento(),
                e.getUbicacion(),
                e.getLocalidad().getLocalidad() + ", " + e.getLocalidad().getProvincia(),
                e.getOrganizador().getNombre(),
                e.getOrganizador().getId(),
                e.getEstado(),
                e.getFechaPublic(),
                artistasConfirmados
        );
    }
}