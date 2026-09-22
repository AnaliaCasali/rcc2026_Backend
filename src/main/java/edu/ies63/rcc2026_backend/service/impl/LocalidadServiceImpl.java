package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.LocalidadCreateDTO;
import edu.ies63.rcc2026_backend.dto.LocalidadResponseDTO;
import edu.ies63.rcc2026_backend.model.Localidad;
import edu.ies63.rcc2026_backend.repository.LocalidadRepository;
import edu.ies63.rcc2026_backend.service.LocalidadService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocalidadServiceImpl implements LocalidadService {

  private final LocalidadRepository localidadRepository;

  public LocalidadServiceImpl(LocalidadRepository localidadRepository) {
    this.localidadRepository = localidadRepository;
  }

  public List<LocalidadResponseDTO> listarLocalidades() {
    return localidadRepository.findAll().stream()
        .map(loc -> new LocalidadResponseDTO(
            loc.getId(), loc.getLocalidad(), loc.getProvincia(),
            loc.getPais(), loc.getLatitud(), loc.getLongitud()
        ))
        .toList();
  }

  @Override
  public LocalidadResponseDTO crearLocalidad(LocalidadCreateDTO dto) {
    Localidad localidad = new Localidad();
    localidad.setLocalidad(dto.localidad());
    localidad.setProvincia(dto.provincia());
    localidad.setPais(dto.pais());
    localidad.setLatitud(dto.latitud());
    localidad.setLongitud(dto.longitud());

    Localidad locGuardada = localidadRepository.save(localidad);

    return new LocalidadResponseDTO(
        locGuardada.getId(),
        locGuardada.getLocalidad(),
        locGuardada.getProvincia(),
        locGuardada.getPais(),
        locGuardada.getLatitud(),
        locGuardada.getLongitud()
    );
  }
}
