package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByPost_IdAndUsuario_Id(Long postId, Long usuarioId);

    Optional<PostLike> findByPost_IdAndUsuario_Id(Long postId, Long usuarioId);

    long countByPost_Id(Long postId);

    // [postId, cantidad de likes] de varios posts a la vez
    @Query("select l.post.id, count(l) from PostLike l where l.post.id in :ids group by l.post.id")
    List<Object[]> contarPorPosts(@Param("ids") Collection<Long> ids);

    // ids de los posts (de la lista) a los que este usuario ya les dio like
    @Query("select l.post.id from PostLike l where l.usuario.id = :usuarioId and l.post.id in :ids")
    List<Long> idsConLikeDe(@Param("usuarioId") Long usuarioId, @Param("ids") Collection<Long> ids);
}