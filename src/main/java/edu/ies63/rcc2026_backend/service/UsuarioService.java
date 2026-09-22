package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.UsuarioResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UsuarioService {
  List<UsuarioResponseDTO> listarUsuarios();
}
