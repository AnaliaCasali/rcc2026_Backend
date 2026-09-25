package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.model.PasswordResetToken;
import edu.ies63.rcc2026_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUsuario(Usuario usuario);
}