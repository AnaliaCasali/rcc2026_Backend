package edu.ies63.rcc2026_backend.model;

import edu.ies63.rcc2026_backend.enums.EstadoEvento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventoTest {

    @Test
    @DisplayName("Debe inicializarse con estado PENDIENTE y lista de grupos vacía no nula por defecto")
    void builder_valoresPorDefecto_deberiaTenerEstadoPendienteYListaVacia() {
        Evento evento = Evento.builder().build();

        assertThat(evento.getEstado()).isEqualTo(EstadoEvento.PENDIENTE);
        assertThat(evento.getGrupos()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Debe permitir agregar participantes a la lista de grupos")
    void agregarGrupo_deberiaAgregarParticipanteCorrectamente() {
        Evento evento = Evento.builder()
                .nombre("Festival de Rock")
                .detalle("Gran festival regional")
                .fecha(LocalDate.of(2026, 11, 20))
                .hora(LocalTime.of(21, 0))
                .build();

        GrupoEvento grupoEvento = GrupoEvento.builder()
                .evento(evento)
                .build();

        evento.getGrupos().add(grupoEvento);

        assertThat(evento.getGrupos()).hasSize(1);
        assertThat(evento.getGrupos().get(0).getEvento()).isEqualTo(evento);
    }
}