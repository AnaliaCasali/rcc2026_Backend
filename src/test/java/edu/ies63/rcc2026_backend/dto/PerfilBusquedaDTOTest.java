package edu.ies63.rcc2026_backend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerfilBusquedaDTOTest {

    @Test
    @DisplayName("Debe instanciar y recuperar todos los campos correctamente")
    void constructorYGetters_FuncionanCorrectamente() {
        PerfilBusquedaDTO dto = new PerfilBusquedaDTO(
                1L,
                "Ballet Folklórico Auka",
                "GRUPO",
                "Danzas folklóricas tradicionales",
                "Las Toscas",
                "https://ejemplo.com/foto.jpg",
                12.5
        );

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.nombre()).isEqualTo("Ballet Folklórico Auka");
        assertThat(dto.rol()).isEqualTo("GRUPO");
        assertThat(dto.biografia()).isEqualTo("Danzas folklóricas tradicionales");
        assertThat(dto.localidad()).isEqualTo("Las Toscas");
        assertThat(dto.fotoPerfil()).isEqualTo("https://ejemplo.com/foto.jpg");
        assertThat(dto.distanciaKm()).isEqualTo(12.5);
    }

    @Test
    @DisplayName("Debe permitir valores nulos para campos opcionales como distancia y foto")
    void camposOpcionalesNulos_Permitidos() {
        PerfilBusquedaDTO dto = new PerfilBusquedaDTO(
                2L,
                "Festival Las Toscas",
                "ORGANIZADOR",
                null,
                null,
                null,
                null
        );

        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.biografia()).isNull();
        assertThat(dto.localidad()).isNull();
        assertThat(dto.fotoPerfil()).isNull();
        assertThat(dto.distanciaKm()).isNull();
    }

    @Test
    @DisplayName("Verifica igualdad por valor (equals y hashCode)")
    void equalsYHashCode_MismosValores_SonIguales() {
        PerfilBusquedaDTO dto1 = new PerfilBusquedaDTO(1L, "Grupo", "GRUPO", "Bio", "Localidad", "foto", 5.0);
        PerfilBusquedaDTO dto2 = new PerfilBusquedaDTO(1L, "Grupo", "GRUPO", "Bio", "Localidad", "foto", 5.0);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.toString()).contains("Grupo", "1", "GRUPO");
    }
}