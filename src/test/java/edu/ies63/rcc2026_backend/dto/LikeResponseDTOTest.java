package edu.ies63.rcc2026_backend.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeResponseDTOTest {

    @Test
    @DisplayName("Instancia y recupera los valores del record correctamente")
    void constructorYGetters_FuncionanCorrectamente() {
        LikeResponseDTO dto = new LikeResponseDTO(5L, true, 12L);

        assertThat(dto.postId()).isEqualTo(5L);
        assertThat(dto.liked()).isTrue();
        assertThat(dto.totalLikes()).isEqualTo(12L);
    }

    @Test
    @DisplayName("Verifica igualdad por valor (equals y hashCode)")
    void equalsYHashCode_MismosValores_SonIguales() {
        LikeResponseDTO dto1 = new LikeResponseDTO(5L, true, 12L);
        LikeResponseDTO dto2 = new LikeResponseDTO(5L, true, 12L);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.toString()).contains("5", "true", "12");
    }
}