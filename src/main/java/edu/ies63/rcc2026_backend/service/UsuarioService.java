package edu.ies63.rcc2026_backend.service;

import edu.ies63.rcc2026_backend.dto.GrupoCreateDTO;
import edu.ies63.rcc2026_backend.dto.GrupoResponseDTO;
import edu.ies63.rcc2026_backend.dto.OrganizadorCreateDTO;
import edu.ies63.rcc2026_backend.dto.OrganizadorResponseDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioCreateDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioResponseDTO;
import edu.ies63.rcc2026_backend.security.dto.AuthResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UsuarioService {
  List<UsuarioResponseDTO> listarUsuarios();
 /* UsuarioResponseDTO crearUsuario(UsuarioCreateDTO usuario);
  OrganizadorResponseDTO crearOrganizador(OrganizadorCreateDTO organizador);
  GrupoResponseDTO crearGrupo(GrupoCreateDTO grupo);
*/
}
