package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.PostCreateDTO;
import edu.ies63.rcc2026_backend.dto.PostResponseDTO;
import edu.ies63.rcc2026_backend.dto.PostUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {
  Page<PostResponseDTO> listarPosts(Pageable pageable, String username);
  Page<PostResponseDTO> listarPremios(Pageable pageable, String username);
  Page<PostResponseDTO> listarPorUsuario(Long usuarioId, Pageable pageable, String username);
  Page<PostResponseDTO> listarFeed(Pageable pageable, String username);

  PostResponseDTO crearPost(PostCreateDTO request, String username);
  PostResponseDTO editarPost(Long id, PostUpdateDTO request, String username);
  void eliminarPost(Long id, String username);
}
