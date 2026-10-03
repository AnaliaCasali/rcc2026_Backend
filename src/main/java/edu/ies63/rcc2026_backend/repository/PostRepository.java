package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.enums.TipoPost;
import edu.ies63.rcc2026_backend.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
  Page<Post> findByTipoPost(TipoPost tipoPost, Pageable pageable);

  // posts de un usuario (perfil)
  Page<Post> findByUsuarioIdAndTipoPost(Long usuarioId, TipoPost tipoPost, Pageable pageable);

  // posts de las cuentas que sigue el usuario (feed)
  @Query("select p from Post p where p.tipoPost = :tipo and p.usuario.id in " +
      "(select s.grupo.id from Seguido s where s.usuario.id = :seguidorId)")
  Page<Post> findFeedDeSeguidos(@Param("tipo") TipoPost tipo,
                                @Param("seguidorId") Long seguidorId,
                                Pageable pageable);
}