package edu.ies63.rcc2026_backend.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacionTest {

    @Test
    @DisplayName("Debe inicializarse con leida en false por defecto con Builder")
    void builder_valoresPorDefecto_deberiaEstarNoLeida() {
        Notificacion notificacion = Notificacion.builder().build();

        assertThat(notificacion.getLeida()).isFalse();
    }

    @Test
    @DisplayName("Debe instanciarse correctamente con todos sus atributos requeridos")
    void builder_creacionCompleta_deberiaAsignarCamposCorrectamente() {
        Usuario usuario = Usuario.builder().id(10L).nombre("Banda Local").build();

        Notificacion notificacion = Notificacion.builder()
                .id(1L)
                .perfil(usuario)
                .mensaje("Has sido invitado al festival")
                .eventoId(5L)
                .leida(false)
                .build();

        assertThat(notificacion.getId()).isEqualTo(1L);
        assertThat(notificacion.getPerfil().getId()).isEqualTo(10L);
        assertThat(notificacion.getMensaje()).isEqualTo("Has sido invitado al festival");
        assertThat(notificacion.getEventoId()).isEqualTo(5L);
        assertThat(notificacion.getLeida()).isFalse();
    }
}