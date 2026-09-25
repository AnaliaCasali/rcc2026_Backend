package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.LocalidadMapResponseDTO;
import edu.ies63.rcc2026_backend.dto.LocalidadResponseDTO;

import java.util.List;

public interface LocalidadService {
  List<LocalidadResponseDTO> listarLocalidades();

  List<LocalidadMapResponseDTO> listarLocalidadesMapa();
}
