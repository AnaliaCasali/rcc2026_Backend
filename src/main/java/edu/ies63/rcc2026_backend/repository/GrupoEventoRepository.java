package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.model.GrupoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoEventoRepository extends JpaRepository<GrupoEvento, Long> {
    List<GrupoEvento> findByArtista_Username(String username);
    Optional<GrupoEvento> findByIdAndArtista_Username(Long id, String username);
    Optional<GrupoEvento> findByEvento_IdAndArtista_Id(Long eventoId, Long artistaId);
    boolean existsByEvento_IdAndArtista_Id(Long eventoId, Long artistaId);
}