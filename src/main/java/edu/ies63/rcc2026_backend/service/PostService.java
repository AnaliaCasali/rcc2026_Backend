package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.PostCreateDTO;
import edu.ies63.rcc2026_backend.dto.PostResponseDTO;
import edu.ies63.rcc2026_backend.dto.PostUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
  Page<PostResponseDTO> listarPosts(Pageable pageable);
  Page<PostResponseDTO> listarPremios(Pageable pageable);

  PostResponseDTO crearPost(PostCreateDTO request, String username);
  PostResponseDTO editarPost(Long id, PostUpdateDTO request, String username);
  void eliminarPost(Long id, String username);
}
