package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.dto.LocalidadResponseDTO;
import edu.ies63.rcc2026_backend.model.Localidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, Long> {

    List<LocalidadResponseDTO> findByLocalidadContainingIgnoreCase(String localidad);
}
