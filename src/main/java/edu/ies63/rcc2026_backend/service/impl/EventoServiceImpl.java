package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.*;
import edu.ies63.rcc2026_backend.enums.EstadoEvento;
import edu.ies63.rcc2026_backend.enums.EstadoInvitacion;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.enums.TipoImagen;
import edu.ies63.rcc2026_backend.exception.EventoConflictException;
import edu.ies63.rcc2026_backend.exception.EventoNoPermitidoException;
import edu.ies63.rcc2026_backend.exception.UsuarioNoEncontradoException;
import edu.ies63.rcc2026_backend.model.*;
import edu.ies63.rcc2026_backend.repository.*;
import edu.ies63.rcc2026_backend.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventoServiceImpl implements EventoService {

    private static final ZoneId ZONA_ARG = ZoneId.of("America/Argentina/Buenos_Aires");

    private final EventoRepository eventoRepository;
    private final GrupoEventoRepository grupoEventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LocalidadRepository localidadRepository;
    private final NotificacionRepository notificacionRepository;

    @Override
    @Transactional
    public EventoDetalleResponseDTO crearEvento(EventoCrearRequestDTO req, String username) {
        Usuario creador = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado: " + username));

        if (creador.getRole() != Role.ORGANIZADOR && !creador.getRole().name().equals("GRUPO")) {
            throw new EventoNoPermitidoException("Solo los organizadores pueden crear eventos");
        }

        LocalDateTime ahora = LocalDateTime.now(ZONA_ARG);
        LocalDateTime fechaHoraEvento = LocalDateTime.of(req.fecha(), req.hora());
        if (!fechaHoraEvento.isAfter(ahora)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha y hora del evento deben ser posteriores a la actual");
        }

        Localidad localidad = localidadRepository.findById(req.localidadId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La localidad no existe"));

        Set<Long> artistaIdsUnicos = new HashSet<>(req.artistaIds());
        if (artistaIdsUnicos.size() != req.artistaIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se permiten artistas repetidos en la invitación");
        }

        List<Usuario> artistas = usuarioRepository.findAllById(artistaIdsUnicos);
        if (artistas.size() != artistaIdsUnicos.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno o más artistas seleccionados no existen");
        }

        for (Usuario a : artistas) {
            if (!"GRUPO".equals(a.getRole().name())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se puede invitar a grupos o artistas");
            }
        }

        boolean autoParticipa = artistaIdsUnicos.contains(creador.getId());
        EstadoEvento estadoInicial = autoParticipa ? EstadoEvento.PUBLICADO : EstadoEvento.PENDIENTE;

        Evento evento = Evento.builder()
                .nombre(req.nombre())
                .detalle(req.detalle())
                .fecha(req.fecha())
                .hora(req.hora())
                .localidad(localidad)
                .direccion(req.direccion())
                .organizador(creador)
                .estado(estadoInicial)
                .grupos(new ArrayList<>())
                .build();

        for (Usuario artista : artistas) {
            boolean esElCreador = artista.getId().equals(creador.getId());

            GrupoEvento ge = GrupoEvento.builder()
                    .evento(evento)
                    .artista(artista)
                    .estadoInvitacion(esElCreador ? EstadoInvitacion.ACEPTADA : EstadoInvitacion.PENDIENTE)
                    .confirmado(esElCreador)
                    .fechaRespuesta(esElCreador ? LocalDateTime.now(ZONA_ARG) : null)
                    .build();

            evento.getGrupos().add(ge);
        }

        Evento guardado = eventoRepository.save(evento);

        for (Usuario artista : artistas) {
            if (!artista.getId().equals(creador.getId())) {
                notificacionRepository.save(Notificacion.builder()
                        .perfil(artista)
                        .eventoId(guardado.getId())
                        .mensaje("Has sido invitado al evento '" + guardado.getNombre() + "' el día " + guardado.getFecha() + ".")
                        .leida(false)
                        .build());
            }
        }

        return mapearADetalleResponse(guardado, username);
    }

    @Override
    @Transactional
    public EventoEdicionResponseDTO editarEvento(Long id, EventoEditarRequestDTO req, String username) {
        if (req.agregarArtistaIds() != null && req.agregarArtistaIds().isEmpty() && !req.tieneCambios()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debes seleccionar una agrupación artística para enviar la invitación");
        }
        if (!req.tieneCambios()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se enviaron cambios");
        }

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El evento no existe"));

        if (!evento.getOrganizador().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar este evento");
        }

        LocalDate hoy = LocalDate.now(ZONA_ARG);
        LocalDateTime ahora = LocalDateTime.now(ZONA_ARG);
        LocalDateTime fechaHoraActual = LocalDateTime.of(evento.getFecha(), evento.getHora());

        if (fechaHoraActual.isBefore(ahora)) {
            throw new EventoConflictException("EVENTO_FINALIZADO", "El evento ya finalizó y no se puede editar");
        }

        LocalDate editableCriticosHasta = evento.getFecha().minusDays(3);
        boolean fueraDeMargenCriticos = hoy.isAfter(editableCriticosHasta);

        List<String> camposModificados = new ArrayList<>();

        if (req.nombre() != null && !req.nombre().isBlank() && !req.nombre().equals(evento.getNombre())) {
            evento.setNombre(req.nombre());
        }
        if (req.detalle() != null && !req.detalle().isBlank() && !req.detalle().equals(evento.getDetalle())) {
            evento.setDetalle(req.detalle());
        }

        boolean intentaEditarCriticos = (req.fecha() != null && !req.fecha().equals(evento.getFecha()))
                || (req.hora() != null && !req.hora().equals(evento.getHora()))
                || (req.localidadId() != null && !req.localidadId().equals(evento.getLocalidad().getId()))
                || (req.direccion() != null && !Objects.equals(req.direccion(), evento.getDireccion()))
                || (req.agregarArtistaIds() != null && !req.agregarArtistaIds().isEmpty())
                || (req.quitarArtistaIds() != null && !req.quitarArtistaIds().isEmpty());

        if (intentaEditarCriticos && fueraDeMargenCriticos) {
            throw new EventoConflictException(
                    "EVENTO_CRITICOS_BLOQUEADOS",
                    "La fecha, hora, lugar y artistas solo se pueden modificar hasta 3 días antes del evento",
                    editableCriticosHasta
            );
        }

        if (req.fecha() != null && !req.fecha().equals(evento.getFecha())) {
            camposModificados.add("FECHA");
            evento.setFecha(req.fecha());
        }
        if (req.hora() != null && !req.hora().equals(evento.getHora())) {
            camposModificados.add("HORA");
            evento.setHora(req.hora());
        }
        if (req.localidadId() != null && !req.localidadId().equals(evento.getLocalidad().getId())) {
            Localidad nuevaLoc = localidadRepository.findById(req.localidadId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La localidad no existe"));
            camposModificados.add("LOCALIDAD");
            evento.setLocalidad(nuevaLoc);
        }
        if (req.direccion() != null && !Objects.equals(req.direccion(), evento.getDireccion())) {
            camposModificados.add("DIRECCION");
            evento.setDireccion(req.direccion().isBlank() ? null : req.direccion());
        }

        if (req.quitarArtistaIds() != null) {
            for (Long aId : req.quitarArtistaIds()) {
                GrupoEvento ge = grupoEventoRepository.findByEvento_IdAndArtista_Id(evento.getId(), aId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El artista no pertenece al evento"));

                if (ge.getEstadoInvitacion() == EstadoInvitacion.ACEPTADA) {
                    throw new EventoConflictException("ARTISTA_CONFIRMADO_NO_QUITABLE", "No se puede quitar a un artista que ya confirmó su participación");
                }
                evento.getGrupos().remove(ge);
                grupoEventoRepository.delete(ge);
            }
        }

        if (req.agregarArtistaIds() != null && !req.agregarArtistaIds().isEmpty()) {
            int artistasActuales = evento.getGrupos().size();
            long nuevosUnicos = req.agregarArtistaIds().stream()
                    .filter(aId -> !grupoEventoRepository.existsByEvento_IdAndArtista_Id(evento.getId(), aId))
                    .distinct()
                    .count();

            if (artistasActuales + nuevosUnicos > 20) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El evento no puede superar el límite de 20 agrupaciones/artistas");
            }

            for (Long aId : req.agregarArtistaIds()) {
                if (grupoEventoRepository.existsByEvento_IdAndArtista_Id(evento.getId(), aId)) {
                    continue;
                }
                Usuario artista = usuarioRepository.findById(aId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Artista no encontrado con ID: " + aId));

                if (!"GRUPO".equals(artista.getRole().name())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden agregar usuarios con rol GRUPO");
                }

                GrupoEvento nuevo = GrupoEvento.builder()
                        .evento(evento)
                        .artista(artista)
                        .estadoInvitacion(EstadoInvitacion.PENDIENTE)
                        .confirmado(false)
                        .build();
                evento.getGrupos().add(nuevo);
                grupoEventoRepository.save(nuevo);

                notificacionRepository.save(Notificacion.builder()
                        .perfil(artista)
                        .eventoId(evento.getId())
                        .mensaje("Has sido invitado al evento '" + evento.getNombre() + "'.")
                        .build());
            }
        }

        if (evento.getGrupos().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El evento debe conservar al menos 1 artista");
        }

        Evento guardado = eventoRepository.save(evento);

        int destinatarios = 0;
        boolean notifEnviada = false;
        if (!camposModificados.isEmpty()) {
            List<GrupoEvento> activos = guardado.getGrupos().stream()
                    .filter(g -> g.getEstadoInvitacion() != EstadoInvitacion.RECHAZADA)
                    .toList();

            for (GrupoEvento g : activos) {
                notificacionRepository.save(Notificacion.builder()
                        .perfil(g.getArtista())
                        .eventoId(guardado.getId())
                        .mensaje("Se modificaron datos del evento '" + guardado.getNombre() + "': " + String.join(", ", camposModificados))
                        .build());
            }
            destinatarios = activos.size();
            notifEnviada = destinatarios > 0;
        }

        EventoDetalleResponseDTO detalleResponse = mapearADetalleResponse(guardado, username);

        EventoEdicionResponseDTO.NotificacionEdicionResumen resumen =
                new EventoEdicionResponseDTO.NotificacionEdicionResumen(notifEnviada, destinatarios, camposModificados);

        return new EventoEdicionResponseDTO(detalleResponse, resumen);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoCalendarioDTO> listarEventosCalendario(int anio, int mes, Long usuarioId, boolean soloSeguidos, String usernameActual) {
        if (mes < 1 || mes > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mes inválido");
        }

        YearMonth ym = YearMonth.of(anio, mes);
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        return eventoRepository.findEventosMesParaUsuario(
                        inicio,
                        fin,
                        EstadoEvento.PUBLICADO,
                        usernameActual,
                        usuarioId,
                        resolverSeguidorId(soloSeguidos, usernameActual)
                ).stream()
                .map(this::mapearACalendario)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventoCalendarioDTO> listarProximos(Long usuarioId, boolean soloSeguidos, String usernameActual, Pageable pageable) {
        LocalDateTime ahora = LocalDateTime.now(ZONA_ARG);

        return eventoRepository.findProximosParaUsuario(
                ahora.toLocalDate(),
                ahora.toLocalTime(),
                EstadoEvento.PUBLICADO,
                usernameActual,
                usuarioId,
                resolverSeguidorId(soloSeguidos, usernameActual),
                pageable
        ).map(this::mapearACalendario);
    }

    private Long resolverSeguidorId(boolean soloSeguidos, String usernameActual) {
        if (!soloSeguidos || usernameActual == null) {
            return null;
        }
        return usuarioRepository.findByUsername(usernameActual)
                .map(Usuario::getId)
                .orElse(null);
    }

    private EventoCalendarioDTO mapearACalendario(Evento e) {
        return new EventoCalendarioDTO(
                e.getId(),
                e.getNombre(),
                e.getFecha(),
                e.getHora(),
                e.getLocalidad().getLocalidad(),
                e.getEstado(),
                e.getOrganizador().getId(),
                e.getOrganizador().getNombre()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public EventoDetalleResponseDTO obtenerDetalleEvento(Long id, String usernameActual) {
        Evento e = eventoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El evento no existe"));

        boolean esOrg = usernameActual != null && e.getOrganizador().getUsername().equals(usernameActual);
        boolean esInvitado = usernameActual != null && e.getGrupos().stream()
                .anyMatch(g -> g.getArtista().getUsername().equals(usernameActual));

        if (e.getEstado() == EstadoEvento.PENDIENTE && !esOrg && !esInvitado) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El evento no existe");
        }

        return mapearADetalleResponse(e, usernameActual);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitacionDetalleDTO> listarMisInvitaciones(String usernameArtista) {
        return grupoEventoRepository.findByArtista_Username(usernameArtista).stream()
                .map(inv -> new InvitacionDetalleDTO(
                        inv.getId(),
                        inv.getEvento().getId(),
                        inv.getEvento().getNombre(),
                        inv.getEvento().getDetalle(),
                        inv.getEvento().getFecha(),
                        inv.getEvento().getHora(),
                        inv.getEvento().getDireccion(),
                        inv.getEvento().getLocalidad().getLocalidad() + ", " + inv.getEvento().getLocalidad().getProvincia(),
                        inv.getEvento().getOrganizador().getNombre(),
                        inv.getEstadoInvitacion()
                ))
                .toList();
    }

    @Override
    @Transactional
    public String responderInvitacion(Long invitacionId, boolean aceptar, String usernameArtista) {
        GrupoEvento invitacion = grupoEventoRepository.findByIdAndArtista_Username(invitacionId, usernameArtista)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitación no encontrada"));

        Evento evento = invitacion.getEvento();
        Usuario organizador = evento.getOrganizador();
        Usuario artista = invitacion.getArtista();

        invitacion.setFechaRespuesta(LocalDateTime.now(ZONA_ARG));

        if (aceptar) {
            invitacion.setEstadoInvitacion(EstadoInvitacion.ACEPTADA);
            invitacion.setConfirmado(true);
            grupoEventoRepository.save(invitacion);

            if (evento.getEstado() == EstadoEvento.PENDIENTE) {
                evento.setEstado(EstadoEvento.PUBLICADO);
                eventoRepository.save(evento);
            }

            notificacionRepository.save(Notificacion.builder()
                    .perfil(organizador)
                    .eventoId(evento.getId())
                    .mensaje("El grupo '" + artista.getNombre() + "' confirmó su asistencia al evento '" + evento.getNombre() + "'.")
                    .build());

            return "Participación confirmada exitosamente";
        } else {
            invitacion.setEstadoInvitacion(EstadoInvitacion.RECHAZADA);
            invitacion.setConfirmado(false);
            grupoEventoRepository.save(invitacion);

            notificacionRepository.save(Notificacion.builder()
                    .perfil(organizador)
                    .eventoId(evento.getId())
                    .mensaje("El grupo '" + artista.getNombre() + "' rechazó la invitación al evento '" + evento.getNombre() + "'.")
                    .build());

            return "Invitación al evento rechazada";
        }
    }

    private EventoDetalleResponseDTO mapearADetalleResponse(Evento e, String usernameActual) {
        boolean esOrg = usernameActual != null && e.getOrganizador().getUsername().equals(usernameActual);
        LocalDate hoy = LocalDate.now(ZONA_ARG);
        LocalDateTime ahora = LocalDateTime.now(ZONA_ARG);
        LocalDateTime fechaHora = LocalDateTime.of(e.getFecha(), e.getHora());

        boolean noHaFinalizado = fechaHora.isAfter(ahora);
        LocalDate editableHasta = e.getFecha().minusDays(3);
        boolean puedeCriticos = esOrg && noHaFinalizado && !hoy.isAfter(editableHasta);

        PermisosEventoDTO permisos = new PermisosEventoDTO(
                esOrg,
                esOrg && noHaFinalizado,
                puedeCriticos,
                editableHasta
        );

        List<ArtistaDetalleDTO> artistas = e.getGrupos().stream()
                .filter(g -> esOrg || g.getEstadoInvitacion() == EstadoInvitacion.ACEPTADA)
                .map(g -> new ArtistaDetalleDTO(
                        g.getId(),
                        g.getArtista().getId(),
                        g.getArtista().getNombre(),
                        obtenerFotoPerfilUrl(g.getArtista()),
                        g.getEstadoInvitacion()
                ))
                .toList();

        return new EventoDetalleResponseDTO(
                e.getId(),
                e.getNombre(),
                e.getDetalle(),
                e.getFecha(),
                e.getHora(),
                new LocalidadSimpleDTO(e.getLocalidad().getId(), e.getLocalidad().getLocalidad()),
                e.getDireccion(),
                e.getEstado(),
                new OrganizadorSimpleDTO(e.getOrganizador().getId(), e.getOrganizador().getNombre(), obtenerFotoPerfilUrl(e.getOrganizador())),
                artistas,
                permisos
        );
    }

    private String obtenerFotoPerfilUrl(Usuario usuario) {
        if (usuario == null || usuario.getImagenesPerfil() == null || usuario.getImagenesPerfil().isEmpty()) {
            return null;
        }

        return usuario.getImagenesPerfil().stream()
                .filter(img -> img.getTipoImagen() == TipoImagen.FOTOPERFIL)
                .map(ImagenPerfil::getUrl)
                .findFirst()
                .orElse(null);
    }
}