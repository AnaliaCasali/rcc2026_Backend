package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.model.Localidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LocalidadRepository extends JpaRepository<Localidad, Long> {

}
