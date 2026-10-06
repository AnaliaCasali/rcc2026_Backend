package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByPerfil_UsernameOrderByFechaCreacionDesc(String username);
    Optional<Notificacion> findByIdAndPerfil_Username(Long id, String username);
    long countByPerfil_UsernameAndLeidaFalse(String username);
}