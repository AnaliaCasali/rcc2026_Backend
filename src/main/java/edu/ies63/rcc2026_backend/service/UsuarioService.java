package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.PerfilResponseDTO;
import edu.ies63.rcc2026_backend.dto.PerfilUpdateDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioResponseDTO;
import edu.ies63.rcc2026_backend.model.Usuario;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UsuarioService {
  List<UsuarioResponseDTO> listarUsuarios();
  PerfilResponseDTO actualizarPerfil(String username, PerfilUpdateDTO request);
  PerfilResponseDTO mapearAPerfilResponseDTO(Usuario usuario);
  PerfilResponseDTO obtenerMiPerfil(String username);
  PerfilResponseDTO verPerfilPublico(Long id);
}
