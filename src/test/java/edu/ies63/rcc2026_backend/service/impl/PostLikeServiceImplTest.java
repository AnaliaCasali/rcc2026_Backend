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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceImplTest {

    @Mock
    private PostLikeRepository likeRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PostLikeServiceImpl postLikeService;

    private Post post;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        post = new Post();
        post.setId(10L);
        post.setTitulo("Publicación de prueba");

        usuario = Usuario.builder()
                .id(1L)
                .username("juan@test.com")
                .nombre("Juan Perez")
                .build();
    }

    @Test
    @DisplayName("Dar Like: Cuando no existe registro previo, crea el like y retorna liked=true")
    void toggleLike_NoExisteLike_CreaNuevoLike() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(usuarioRepository.findByUsername("juan@test.com")).thenReturn(Optional.of(usuario));
        when(likeRepository.findByPost_IdAndUsuario_Id(10L, 1L)).thenReturn(Optional.empty());
        when(likeRepository.countByPost_Id(10L)).thenReturn(1L);

        LikeResponseDTO resultado = postLikeService.toggleLike(10L, "juan@test.com");

        assertThat(resultado.postId()).isEqualTo(10L);
        assertThat(resultado.liked()).isTrue();
        assertThat(resultado.totalLikes()).isEqualTo(1L);

        ArgumentCaptor<PostLike> likeCaptor = ArgumentCaptor.forClass(PostLike.class);
        verify(likeRepository).save(likeCaptor.capture());
        assertThat(likeCaptor.getValue().getPost()).isEqualTo(post);
        assertThat(likeCaptor.getValue().getUsuario()).isEqualTo(usuario);
        verify(likeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Quitar Like: Cuando ya existe un registro previo, lo elimina y retorna liked=false")
    void toggleLike_ExisteLike_EliminaLike() {
        PostLike likeExistente = PostLike.builder()
                .id(100L)
                .post(post)
                .usuario(usuario)
                .build();

        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(usuarioRepository.findByUsername("juan@test.com")).thenReturn(Optional.of(usuario));
        when(likeRepository.findByPost_IdAndUsuario_Id(10L, 1L)).thenReturn(Optional.of(likeExistente));
        when(likeRepository.countByPost_Id(10L)).thenReturn(0L);

        LikeResponseDTO resultado = postLikeService.toggleLike(10L, "juan@test.com");

        assertThat(resultado.postId()).isEqualTo(10L);
        assertThat(resultado.liked()).isFalse();
        assertThat(resultado.totalLikes()).isEqualTo(0L);

        verify(likeRepository).delete(likeExistente);
        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Post inexistente: Lanza PostNotFoundException y no interactúa con repositorios posteriores")
    void toggleLike_PostInexistente_LanzaPostNotFoundException() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postLikeService.toggleLike(999L, "juan@test.com"))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage("No se encontró el post con ID: 999");

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(likeRepository);
    }

    @Test
    @DisplayName("Lanza UsuarioNoEncontradoException si el username no existe")
    void toggleLike_UsuarioInexistente_LanzaUsuarioNoEncontradoException() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(usuarioRepository.findByUsername("fantasma@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postLikeService.toggleLike(1L, "fantasma@test.com"))
                .isInstanceOf(UsuarioNoEncontradoException.class)
                .hasMessageContaining("fantasma@test.com");

        verifyNoInteractions(likeRepository);
    }
}