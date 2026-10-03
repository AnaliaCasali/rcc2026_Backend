package edu.ies63.rcc2026_backend.dto;

import edu.ies63.rcc2026_backend.model.Localidad;
import edu.ies63.rcc2026_backend.model.Post;
import edu.ies63.rcc2026_backend.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BusquedaResponseDTOTest {

    @Test
    @DisplayName("Debe instanciar y acceder a las listas de perfiles, posts y localidades")
    void constructorYGetters_FuncionanCorrectamente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Grupo A");

        Post post = new Post();
        post.setId(10L);
        post.setTitulo("Noticia");

        Localidad localidad = new Localidad();
        localidad.setId(100L);
        localidad.setLocalidad("Las Toscas");

        BusquedaResponseDTO dto = new BusquedaResponseDTO(
                List.of(usuario),
                List.of(post),
                List.of(localidad)
        );

        assertThat(dto.perfiles()).containsExactly(usuario);
        assertThat(dto.posts()).containsExactly(post);
        assertThat(dto.localidades()).containsExactly(localidad);
    }

    @Test
    @DisplayName("Debe manejar listas vacías sin errores")
    void listasVacias_Permitidas() {
        BusquedaResponseDTO dto = new BusquedaResponseDTO(
                List.of(),
                List.of(),
                List.of()
        );

        assertThat(dto.perfiles()).isEmpty();
        assertThat(dto.posts()).isEmpty();
        assertThat(dto.localidades()).isEmpty();
    }

    @Test
    @DisplayName("Verifica equals y hashCode sobre las colecciones")
    void equalsYHashCode_ListasIdenticas_SonIguales() {
        BusquedaResponseDTO dto1 = new BusquedaResponseDTO(List.of(), List.of(), List.of());
        BusquedaResponseDTO dto2 = new BusquedaResponseDTO(List.of(), List.of(), List.of());

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }
}