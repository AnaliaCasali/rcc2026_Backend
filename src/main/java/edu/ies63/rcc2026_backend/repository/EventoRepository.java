package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.enums.EstadoEvento;
import edu.ies63.rcc2026_backend.model.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    String ACEPTADA = "edu.ies63.rcc2026_backend.enums.EstadoInvitacion.ACEPTADA";

    String FILTROS =
            "(e.activo = true OR e.organizador.username = :username) " +
                    "AND (e.estado = :estadoPublicado OR e.organizador.username = :username) " +
                    "AND (:usuarioId IS NULL OR e.organizador.id = :usuarioId " +
                    "     OR EXISTS (SELECT g FROM GrupoEvento g WHERE g.evento = e AND g.artista.id = :usuarioId AND g.estadoInvitacion = " + ACEPTADA + ")) " +
                    "AND (:seguidorId IS NULL OR e.organizador.id = :seguidorId " +
                    "     OR e.organizador.id IN (SELECT s.grupo.id FROM Seguido s WHERE s.usuario.id = :seguidorId) " +
                    "     OR EXISTS (SELECT g2 FROM GrupoEvento g2 WHERE g2.evento = e AND g2.estadoInvitacion = " + ACEPTADA + " " +
                    "                AND (g2.artista.id = :seguidorId OR g2.artista.id IN (SELECT s2.grupo.id FROM Seguido s2 WHERE s2.usuario.id = :seguidorId))))";

    String PROXIMOS = "(e.fecha > :hoy OR (e.fecha = :hoy AND e.hora >= :ahora)) AND " + FILTROS;

    @Query("SELECT e FROM Evento e WHERE e.fecha BETWEEN :inicio AND :fin AND " + FILTROS + " ORDER BY e.fecha ASC, e.hora ASC")
    List<Evento> findEventosMesParaUsuario(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin,
            @Param("estadoPublicado") EstadoEvento estadoPublicado,
            @Param("username") String username,
            @Param("usuarioId") Long usuarioId,
            @Param("seguidorId") Long seguidorId
    );

    @Query(value = "SELECT e FROM Evento e WHERE " + PROXIMOS + " ORDER BY e.fecha ASC, e.hora ASC",
            countQuery = "SELECT COUNT(e) FROM Evento e WHERE " + PROXIMOS)
    Page<Evento> findProximosParaUsuario(
            @Param("hoy") LocalDate hoy,
            @Param("ahora") LocalTime ahora,
            @Param("estadoPublicado") EstadoEvento estadoPublicado,
            @Param("username") String username,
            @Param("usuarioId") Long usuarioId,
            @Param("seguidorId") Long seguidorId,
            Pageable pageable
    );
}
