package edu.ies63.rcc2026_backend.model;

import edu.ies63.rcc2026_backend.enums.EstadoInvitacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrupoEventoTest {

    @Test
    @DisplayName("Debe inicializarse con estado PENDIENTE y confirmado en false por defecto con Builder")
    void builder_valoresPorDefecto_deberiaTenerEstadoPendienteYNoConfirmado() {
        GrupoEvento grupoEvento = GrupoEvento.builder().build();

        assertThat(grupoEvento.getEstadoInvitacion()).isEqualTo(EstadoInvitacion.PENDIENTE);
        assertThat(grupoEvento.getConfirmado()).isFalse();
    }

    @Test
    @DisplayName("sincronizarConfirmado() debe setear confirmado en true cuando el estado sea ACEPTADA")
    void sincronizarConfirmado_conEstadoAceptada_deberiaSerTrue() {
        GrupoEvento grupoEvento = GrupoEvento.builder()
                .estadoInvitacion(EstadoInvitacion.ACEPTADA)
                .confirmado(false)
                .build();

        grupoEvento.sincronizarConfirmado();

        assertThat(grupoEvento.getConfirmado()).isTrue();
    }

    @Test
    @DisplayName("sincronizarConfirmado() debe setear confirmado en false cuando el estado sea PENDIENTE o RECHAZADA")
    void sincronizarConfirmado_conEstadoNoAceptada_deberiaSerFalse() {
        GrupoEvento invitacionPendiente = GrupoEvento.builder()
                .estadoInvitacion(EstadoInvitacion.PENDIENTE)
                .confirmado(true) // Forzamos un valor erróneo para validar que el callback lo corrija
                .build();

        invitacionPendiente.sincronizarConfirmado();
        assertThat(invitacionPendiente.getConfirmado()).isFalse();

        GrupoEvento invitacionRechazada = GrupoEvento.builder()
                .estadoInvitacion(EstadoInvitacion.RECHAZADA)
                .confirmado(true)
                .build();

        invitacionRechazada.sincronizarConfirmado();
        assertThat(invitacionRechazada.getConfirmado()).isFalse();
    }
}