package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByUsername(String username);
}
