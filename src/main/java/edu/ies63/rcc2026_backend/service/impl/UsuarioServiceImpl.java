package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.UsuarioResponseDTO;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UsuarioServiceImpl implements UsuarioService, UserDetailsService {
  private final UsuarioRepository usuarioRepository;

  public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
    return new UsuarioResponseDTO(
        usuario.getId(),
        usuario.getUsername(),
        usuario.getRole(),
        usuario.getNombre(),
        usuario.getBiografia(),
        usuario.getDirector(),
        usuario.getLocalidad().getId());
  }

  @Override
  public List<UsuarioResponseDTO> listarUsuarios() {
    List<UsuarioResponseDTO> lista =
        usuarioRepository
            .findAll()
            .stream()
            .map(this::toResponseDTO)
            .toList();
    return lista;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return usuarioRepository.findByUsername(username)
        .orElseThrow(() ->
            new UsernameNotFoundException("Usuario no encontrado: " + username));
  }
}
