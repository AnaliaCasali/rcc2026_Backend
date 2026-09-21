package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.GrupoCreateDTO;
import edu.ies63.rcc2026_backend.dto.GrupoResponseDTO;
import edu.ies63.rcc2026_backend.dto.OrganizadorCreateDTO;
import edu.ies63.rcc2026_backend.dto.OrganizadorResponseDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioCreateDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioResponseDTO;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UsuarioServiceImpl implements edu.ies63.rcc2026_backend.service.UsuarioService, UserDetailsService {
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
        usuario.getBiografia());
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
  public UsuarioResponseDTO crearUsuario(UsuarioCreateDTO usuario) {
    Usuario usuarioEntity = new Usuario();

    usuarioEntity.setNombre(usuario.nombre());
    usuarioEntity.setUsername(usuario.username());
    usuarioEntity.setPassword(usuario.password());
    usuarioEntity.setRole(Role.USUARIO);
    usuarioEntity.setBiografia(usuario.biografia());
    usuarioEntity.setDebaja(false);

    Usuario usuarioGuardado = usuarioRepository.save(usuarioEntity);
    return new UsuarioResponseDTO(
        usuarioGuardado.getId(),
        usuarioGuardado.getUsername(),
        usuarioGuardado.getRole(),
        usuarioGuardado.getNombre(),
        usuarioGuardado.getBiografia());
  }

  @Override
  public OrganizadorResponseDTO crearOrganizador(OrganizadorCreateDTO organizador) {
    Usuario orgEntity = new Usuario();

    orgEntity.setNombre(organizador.nombre());
    orgEntity.setUsername(organizador.username());
    orgEntity.setPassword(organizador.password());
    orgEntity.setRole(Role.ORGANIZADOR);
    orgEntity.setBiografia(organizador.biografia());
    orgEntity.setDebaja(false);

    Usuario organizadorGuardado = usuarioRepository.save(orgEntity);
    return new OrganizadorResponseDTO(
        organizadorGuardado.getId(),
        organizadorGuardado.getUsername(),
        organizadorGuardado.getRole(),
        organizadorGuardado.getNombre(),
        organizadorGuardado.getBiografia(),
        organizadorGuardado.getDirector(),
        organizadorGuardado.getLocalidad());
  }

  @Override
  public GrupoResponseDTO crearGrupo(GrupoCreateDTO grupo) {
    Usuario grupoEntity = new Usuario();

    grupoEntity.setNombre(grupo.nombre());
    grupoEntity.setUsername(grupo.username());
    grupoEntity.setPassword(grupo.password());
    grupoEntity.setRole(Role.GRUPO);
    grupoEntity.setBiografia(grupo.biografia());
    grupoEntity.setDebaja(false);

    Usuario grupoGuardado = usuarioRepository.save(grupoEntity);
    return new GrupoResponseDTO(
        grupoGuardado.getId(),
        grupoGuardado.getUsername(),
        grupoGuardado.getRole(),
        grupoGuardado.getNombre(),
        grupoGuardado.getBiografia(),
        grupoGuardado.getDirector(),
        grupoGuardado.getLocalidad());
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return usuarioRepository.findByUsername(username)
            .orElseThrow(() ->
                    new UsernameNotFoundException("Usuario no encontrado: " + username));
  }
}
