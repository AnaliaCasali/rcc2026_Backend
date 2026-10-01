package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.LikeResponseDTO;
import edu.ies63.rcc2026_backend.exception.PostNotFoundException;
import edu.ies63.rcc2026_backend.exception.UsuarioNoEncontradoException;
import edu.ies63.rcc2026_backend.model.PostLike;
import edu.ies63.rcc2026_backend.model.Post;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.PostLikeRepository;
import edu.ies63.rcc2026_backend.repository.PostRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public LikeResponseDTO toggleLike(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("No se encontró el post con ID: " + postId));

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsuarioNoEncontradoException("No se encontró el usuario: " + username));

        Optional<PostLike> likeExistente = likeRepository.findByPost_IdAndUsuario_Id(postId, usuario.getId());

        boolean dioLike;
        if (likeExistente.isPresent()) {
            // Ya le había dado me gusta: se quita
            likeRepository.delete(likeExistente.get());
            dioLike = false;
        } else {
            // No le había dado me gusta: se registra
            PostLike nuevoLike = PostLike.builder()
                    .post(post)
                    .usuario(usuario)
                    .build();
            likeRepository.save(nuevoLike);
            dioLike = true;
        }

        long totalLikes = likeRepository.countByPost_Id(postId);

        return new LikeResponseDTO(postId, dioLike, totalLikes);
    }
}