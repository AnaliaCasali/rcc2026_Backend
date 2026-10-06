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
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    Page<Evento> findByEstado(EstadoEvento estado, Pageable pageable);

    @Query("SELECT e FROM Evento e WHERE e.estado = :estado " +
            "AND e.fechaEvento BETWEEN :inicio AND :fin " +
            "ORDER BY e.fechaEvento ASC, e.horaEvento ASC")
    List<Evento> findByEstadoAndFechaEventoBetween(
            @Param("estado") EstadoEvento estado,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin
    );
}