package edu.ies63.rcc2026_backend.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PostLikeTest {

    @Test
    @DisplayName("Debe instanciar mediante Builder y leer todas sus propiedades")
    void testBuilderYGetters() {
        Usuario usuario = Usuario.builder().id(1L).username("usuario@test.com").build();
        Post post = new Post();
        post.setId(10L);
        LocalDateTime ahora = LocalDateTime.now();

        PostLike like = PostLike.builder()
                .id(100L)
                .usuario(usuario)
                .post(post)
                .activo(true)
                .fechaLike(ahora)
                .build();

        assertThat(like.getId()).isEqualTo(100L);
        assertThat(like.getUsuario()).isEqualTo(usuario);
        assertThat(like.getPost()).isEqualTo(post);
        assertThat(like.isActivo()).isTrue();
        assertThat(like.getFechaLike()).isEqualTo(ahora);
    }

    @Test
    @DisplayName("Debe permitir instanciar mediante constructor vacío y asignar campos con setters")
    void testNoArgsConstructorYSetters() {
        PostLike like = new PostLike();
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        Post post = new Post();
        post.setId(20L);
        LocalDateTime ahora = LocalDateTime.now();

        like.setId(200L);
        like.setUsuario(usuario);
        like.setPost(post);
        like.setActivo(false);
        like.setFechaLike(ahora);

        assertThat(like.getId()).isEqualTo(200L);
        assertThat(like.getUsuario()).isEqualTo(usuario);
        assertThat(like.getPost()).isEqualTo(post);
        assertThat(like.isActivo()).isFalse();
        assertThat(like.getFechaLike()).isEqualTo(ahora);
    }

    @Test
    @DisplayName("Debe instanciar mediante AllArgsConstructor")
    void testAllArgsConstructor() {
        Usuario usuario = new Usuario();
        Post post = new Post();
        LocalDateTime ahora = LocalDateTime.now();

        PostLike like = new PostLike(300L, usuario, post, true, ahora);

        assertThat(like.getId()).isEqualTo(300L);
        assertThat(like.getUsuario()).isEqualTo(usuario);
        assertThat(like.getPost()).isEqualTo(post);
        assertThat(like.isActivo()).isTrue();
        assertThat(like.getFechaLike()).isEqualTo(ahora);
    }

    @Test
    @DisplayName("Verifica igualdad, hashCode y toString generados por Lombok")
    void testEqualsHashCodeToString() {
        Usuario usuario = new Usuario();
        Post post = new Post();
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 2, 16, 0);

        PostLike like1 = new PostLike(1L, usuario, post, true, fecha);
        PostLike like2 = new PostLike(1L, usuario, post, true, fecha);
        PostLike like3 = new PostLike(2L, usuario, post, false, fecha);

        assertThat(like1).isEqualTo(like2);
        assertThat(like1.hashCode()).isEqualTo(like2.hashCode());
        assertThat(like1).isNotEqualTo(like3);
        assertThat(like1.toString()).contains("PostLike", "id=1", "activo=true");
    }
}