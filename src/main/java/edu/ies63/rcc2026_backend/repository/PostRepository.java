package edu.ies63.rcc2026_backend.repository;

import edu.ies63.rcc2026_backend.enums.TipoPost;
import edu.ies63.rcc2026_backend.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
  Page<Post> findByTipoPost(TipoPost tipoPost, Pageable pageable);
}
