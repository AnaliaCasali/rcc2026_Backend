package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.model.Post;
import edu.ies63.rcc2026_backend.model.PostLike;
import edu.ies63.rcc2026_backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByPost_IdAndUsuario_Id(Long postId, Long usuarioId);

    Optional<PostLike> findByPost_IdAndUsuario_Id(Long postId, Long usuarioId);

    long countByPost_Id(Long postId);
}