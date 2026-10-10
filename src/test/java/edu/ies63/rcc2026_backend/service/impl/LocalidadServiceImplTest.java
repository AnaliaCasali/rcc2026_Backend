package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.LocalidadMapResponseDTO;
import edu.ies63.rcc2026_backend.dto.LocalidadResponseDTO;
import edu.ies63.rcc2026_backend.enums.Pais;
import edu.ies63.rcc2026_backend.enums.Provincia;
import edu.ies63.rcc2026_backend.model.Localidad;
import edu.ies63.rcc2026_backend.repository.LocalidadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalidadServiceImplTest {

  @Mock
  private LocalidadRepository localidadRepository;

  @InjectMocks
  private LocalidadServiceImpl localidadService;

  private Localidad localidadMock;

  @BeforeEach
  void setUp() {
    localidadMock = new Localidad();
    localidadMock.setId(1L);
    localidadMock.setLocalidad("Resistencia");
    localidadMock.setProvincia(Provincia.CHACO);
    localidadMock.setPais(Pais.ARGENTINA);
    localidadMock.setLatitud(-27.4514);
    localidadMock.setLongitud(-58.9867);
  }

  // ---------- listarLocalidades ----------

  @Test
  void listarLocalidades_DebeRetornarListaMapeada() {
    // Arrange
    when(localidadRepository.findAllByOrderByLocalidadAsc()).thenReturn(List.of(localidadMock));
    // Act
    List<LocalidadResponseDTO> result = localidadService.listarLocalidades();
    // Assert
    assertEquals(1, result.size());
    LocalidadResponseDTO dto = result.get(0);
    assertEquals(1L, dto.id());
    assertEquals("Resistencia", dto.localidad());
    verify(localidadRepository, times(1)).findAllByOrderByLocalidadAsc();
  }

  @Test
  void listarLocalidades_ConVariasLocalidades_DebeMapearTodas() {
    // Arrange
    Localidad otra = new Localidad();
    otra.setId(2L);
    otra.setLocalidad("Corrientes");
    otra.setProvincia(Provincia.CORRIENTES);
    otra.setPais(Pais.ARGENTINA);
    when(localidadRepository.findAllByOrderByLocalidadAsc()).thenReturn(List.of(localidadMock, otra));

    // Act
    List<LocalidadResponseDTO> result = localidadService.listarLocalidades();

    // Assert
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(d -> d.id().equals(1L) && d.localidad().equals("Resistencia")));
    assertTrue(result.stream().anyMatch(d -> d.id().equals(2L) && d.localidad().equals("Corrientes")));
  }

  @Test
  void listarLocalidades_SinLocalidades_DebeRetornarListaVacia() {
    // Arrange
    when(localidadRepository.findAllByOrderByLocalidadAsc()).thenReturn(List.of());
    // Act
    List<LocalidadResponseDTO> result = localidadService.listarLocalidades();
    // Assert
    assertTrue(result.isEmpty());
    verify(localidadRepository, times(1)).findAllByOrderByLocalidadAsc();
  }

  // ---------- listarLocalidadesMapa ----------

  @Test
  void listarLocalidadesMapa_DebeRetornarListaMapeadaConCoordenadas() {
    // Arrange
    when(localidadRepository.findAll()).thenReturn(List.of(localidadMock));

    // Act
    List<LocalidadMapResponseDTO> result = localidadService.listarLocalidadesMapa();

    // Assert
    assertEquals(1, result.size());
    LocalidadMapResponseDTO dto = result.get(0);
    assertEquals(1L, dto.id());
    assertEquals("Resistencia", dto.localidad());
    assertEquals(-27.4514, dto.latitud());
    assertEquals(-58.9867, dto.longitud());
    verify(localidadRepository, times(1)).findAll();
  }

  @Test
  void listarLocalidadesMapa_ConCoordenadasNulas_DebeMapearConNull() {
    // Arrange: localidad cargada sin latitud/longitud (campos no obligatorios en la entidad)
    Localidad sinCoordenadas = new Localidad();
    sinCoordenadas.setId(3L);
    sinCoordenadas.setLocalidad("Sin coordenadas");
    sinCoordenadas.setProvincia(Provincia.CHACO);
    sinCoordenadas.setPais(Pais.ARGENTINA);

    when(localidadRepository.findAll()).thenReturn(List.of(sinCoordenadas));

    // Act
    List<LocalidadMapResponseDTO> result = localidadService.listarLocalidadesMapa();

    // Assert
    assertNull(result.get(0).latitud());
    assertNull(result.get(0).longitud());
  }

  @Test
  void listarLocalidadesMapa_SinLocalidades_DebeRetornarListaVacia() {
    // Arrange
    when(localidadRepository.findAll()).thenReturn(List.of());

    // Act
    List<LocalidadMapResponseDTO> result = localidadService.listarLocalidadesMapa();

    // Assert
    assertTrue(result.isEmpty());
  }
}