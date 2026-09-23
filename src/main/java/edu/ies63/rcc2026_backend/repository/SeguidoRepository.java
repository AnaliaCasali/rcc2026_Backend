package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.model.Seguido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeguidoRepository extends JpaRepository<Seguido, Long> {

  boolean existsByUsuarioIdAndGrupoId(
      Long usuarioId,
      Long grupoId
  );

  Optional<Seguido> findByUsuarioIdAndGrupoId(
      Long usuarioId,
      Long grupoId
  );

  long countByGrupoId(Long grupoId);
}