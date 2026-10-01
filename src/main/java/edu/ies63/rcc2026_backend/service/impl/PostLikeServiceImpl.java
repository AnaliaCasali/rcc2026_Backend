package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.LikeResponseDTO;
import edu.ies63.rcc2026_backend.exception.PostNotFoundException;
import edu.ies63.rcc2026_backend.exception.UsuarioNoEncontradoException;
import edu.ies63.rcc2026_backend.model.Post;
import edu.ies63.rcc2026_backend.model.PostLike;
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

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public LikeResponseDTO toggleLike(Long postId, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsuarioNoEncontradoException(username));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        Optional<PostLike> likeExistente = postLikeRepository.findByUsuarioAndPost(usuario, post);

        boolean nuevoEstado;
        if (likeExistente.isPresent()) {
            PostLike like = likeExistente.get();
            nuevoEstado = !like.isActivo();
            like.setActivo(nuevoEstado);
            postLikeRepository.save(like);
        } else {
            nuevoEstado = true;
            PostLike nuevoLike = PostLike.builder()
                    .usuario(usuario)
                    .post(post)
                    .activo(true)
                    .build();
            postLikeRepository.save(nuevoLike);
        }

        long totalLikes = postLikeRepository.countByPostAndActivoTrue(post);
        return new LikeResponseDTO(nuevoEstado, totalLikes);
    }
}