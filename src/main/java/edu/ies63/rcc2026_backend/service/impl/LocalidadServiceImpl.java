package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.model.Localidad;
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
  public List<Localidad> listarLocalidades() {
    return localidadRepository.findAll();
  }
}