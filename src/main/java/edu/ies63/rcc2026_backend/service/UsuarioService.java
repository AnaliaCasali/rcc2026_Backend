package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.PerfilResponseDTO;
import edu.ies63.rcc2026_backend.dto.PerfilUpdateDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioResponseDTO;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.security.dto.CambiarPasswordRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
public interface UsuarioService {
  Page<UsuarioResponseDTO> listarUsuarios(Pageable pageable);
  PerfilResponseDTO actualizarPerfil(String username, PerfilUpdateDTO request);
  PerfilResponseDTO mapearAPerfilResponseDTO(Usuario usuario);
  PerfilResponseDTO obtenerMiPerfil(String username);
  PerfilResponseDTO verPerfilPublico(Long id);
  void cambiarPassword(CambiarPasswordRequest request, Principal connectedUser);
}
