package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.enums.EstadoEvento;
import edu.ies63.rcc2026_backend.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    @Query("SELECT e FROM Evento e WHERE e.fecha BETWEEN :inicio AND :fin " +
            "AND (e.estado = :estadoPublicado OR e.organizador.username = :username) " +
            "ORDER BY e.fecha ASC, e.hora ASC")
    List<Evento> findEventosMesParaUsuario(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin,
            @Param("estadoPublicado") EstadoEvento estadoPublicado,
            @Param("username") String username
    );
}