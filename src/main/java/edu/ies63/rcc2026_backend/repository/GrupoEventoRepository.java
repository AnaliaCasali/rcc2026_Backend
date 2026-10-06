package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.model.GrupoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoEventoRepository extends JpaRepository<GrupoEvento, Long> {
    List<GrupoEvento> findByEvento_Id(Long eventoId);
    Optional<GrupoEvento> findByEvento_IdAndArtista_Username(Long eventoId, String username);
    boolean existsByEvento_IdAndConfirmadoTrue(Long eventoId);
}