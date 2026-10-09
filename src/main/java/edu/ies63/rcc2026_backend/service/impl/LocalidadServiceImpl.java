package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.LocalidadMapResponseDTO;
import edu.ies63.rcc2026_backend.dto.LocalidadResponseDTO;
import edu.ies63.rcc2026_backend.repository.LocalidadRepository;
import edu.ies63.rcc2026_backend.service.LocalidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalidadServiceImpl implements LocalidadService {

  private final LocalidadRepository localidadRepository;

  @Override
  public List<LocalidadResponseDTO> listarLocalidades() {

    return localidadRepository.findAllByOrderByLocalidadAsc()
        .stream()
        .map(l -> new LocalidadResponseDTO(
            l.getId(),
            l.getLocalidad()
        ))
        .toList();
  }

  @Override
  public List<LocalidadMapResponseDTO> listarLocalidadesMapa() {

    return localidadRepository.findAll()
        .stream()
        .map(l -> new LocalidadMapResponseDTO(
            l.getId(),
            l.getLocalidad(),
            l.getLatitud(),
            l.getLongitud()
        ))
        .toList();
  }
}