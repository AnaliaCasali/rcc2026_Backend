package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.LocalidadCreateDTO;
import edu.ies63.rcc2026_backend.dto.LocalidadResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LocalidadService {
  LocalidadResponseDTO crearLocalidad(LocalidadCreateDTO dto);
  List<LocalidadResponseDTO> listarLocalidades();
}
