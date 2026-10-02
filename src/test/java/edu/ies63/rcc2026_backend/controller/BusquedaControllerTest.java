package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.PerfilBusquedaDTO;
import edu.ies63.rcc2026_backend.exception.BusquedaInvalidaException;
import edu.ies63.rcc2026_backend.service.BusquedaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusquedaControllerTest {

    @Mock
    private BusquedaService busquedaService;

    @InjectMocks
    private BusquedaController busquedaController;

    @Test
    @DisplayName("Llama a busquedaService con parámetros válidos y retorna 200 OK")
    void buscar_ParametrosValidos_Retorna200() {
        PerfilBusquedaDTO mockPerfil = new PerfilBusquedaDTO(
                1L, "Danzas Auka", "GRUPO", "Bio", "Las Toscas", null, null
        );
        when(busquedaService.buscar("auka", null, null, null, null, null))
                .thenReturn(List.of(mockPerfil));

        ResponseEntity<List<PerfilBusquedaDTO>> response = busquedaController.buscar(
                "auka", null, null, null, null, null
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(mockPerfil);
        verify(busquedaService).buscar("auka", null, null, null, null, null);
    }

    @Test
    @DisplayName("Lanza BusquedaInvalidaException cuando se envía lat sin lng")
    void buscar_LatSinLng_LanzaExcepcion() {
        assertThatThrownBy(() -> busquedaController.buscar(null, null, -28.35, null, 10, null))
                .isInstanceOf(BusquedaInvalidaException.class)
                .hasMessage("lat y lng deben enviarse juntos");

        verifyNoInteractions(busquedaService);
    }

    @Test
    @DisplayName("Lanza BusquedaInvalidaException cuando se envía lng sin lat")
    void buscar_LngSinLat_LanzaExcepcion() {
        assertThatThrownBy(() -> busquedaController.buscar(null, null, null, -59.26, 10, null))
                .isInstanceOf(BusquedaInvalidaException.class)
                .hasMessage("lat y lng deben enviarse juntos");

        verifyNoInteractions(busquedaService);
    }

    @Test
    @DisplayName("Lanza BusquedaInvalidaException cuando falta radioKm o es negativo")
    void buscar_GpsSinRadioValido_LanzaExcepcion() {
        assertThatThrownBy(() -> busquedaController.buscar(null, null, -28.35, -59.26, -5, null))
                .isInstanceOf(BusquedaInvalidaException.class)
                .hasMessage("radioKm debe ser un valor positivo al usar GPS");

        verifyNoInteractions(busquedaService);
    }
}