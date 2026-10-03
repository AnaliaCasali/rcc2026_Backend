package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.localidad WHERE u.debaja = false AND u.role IN :roles")
    List<Usuario> findByDebajaFalseAndRoleIn(Collection<Role> roles);
}
