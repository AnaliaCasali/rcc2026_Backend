package edu.ies63.rcc2026_backend.controller;

import edu.ies63.rcc2026_backend.dto.LocalidadMapResponseDTO;
import edu.ies63.rcc2026_backend.dto.LocalidadResponseDTO;
import edu.ies63.rcc2026_backend.service.LocalidadService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalidadControllerTest {

  @Mock
  private LocalidadService localidadService;

  @InjectMocks
  private LocalidadController localidadController;

  @Test
  @DisplayName("listar retorna 200 OK y lista de localidades")
  void listar_DebeRetornarListaDeLocalidades() {
    List<LocalidadResponseDTO> lista = List.of(new LocalidadResponseDTO(1L, "Localidad"));
    when(localidadService.listarLocalidades()).thenReturn(lista);

    ResponseEntity<List<LocalidadResponseDTO>> response = localidadController.listar();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
    verify(localidadService).listarLocalidades();
  }

  @Test
  @DisplayName("listarMapa retorna 200 OK y lista con coordenadas")
  void listarMapa_DebeRetornarListaParaMapa() {
    List<LocalidadMapResponseDTO> lista = List.of(new LocalidadMapResponseDTO(1L, "Loc", 1.0, 1.0));
    when(localidadService.listarLocalidadesMapa()).thenReturn(lista);

    ResponseEntity<List<LocalidadMapResponseDTO>> response = localidadController.listarMapa();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
    verify(localidadService).listarLocalidadesMapa();
  }
}