package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.enums.EstadoEvento;
import edu.ies63.rcc2026_backend.model.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    Page<Evento> findByEstado(EstadoEvento estado, Pageable pageable);
}