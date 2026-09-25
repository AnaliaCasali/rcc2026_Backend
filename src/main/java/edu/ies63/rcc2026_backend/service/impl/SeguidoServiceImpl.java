package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.SeguidoResponseDTO;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.exception.AutoSeguimientoException;
import edu.ies63.rcc2026_backend.exception.GrupoNoEncontradoException;
import edu.ies63.rcc2026_backend.exception.NoEsGrupoException;
import edu.ies63.rcc2026_backend.exception.UsuarioNoEncontradoException;
import edu.ies63.rcc2026_backend.model.Seguido;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.SeguidoRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.service.SeguidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeguidoServiceImpl implements SeguidoService {

  private final SeguidoRepository seguidoRepository;
  private final UsuarioRepository usuarioRepository;

  @Override
  public boolean toggleSeguimiento(Long usuarioId, Long grupoId) {
    if (usuarioId.equals(grupoId)) {
      throw new AutoSeguimientoException();
    }

    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

    Usuario grupo = usuarioRepository.findById(grupoId)
        .orElseThrow(() -> new GrupoNoEncontradoException(grupoId));

    if (grupo.getRole() != Role.GRUPO) {
      throw new NoEsGrupoException(grupoId);
    }

    var seguido = seguidoRepository.findByUsuarioIdAndGrupoId(usuarioId, grupoId);

    if (seguido.isPresent()) {
      seguidoRepository.delete(seguido.get());
      return false;
    }

    Seguido nuevoSeguido = Seguido.builder()
        .usuario(usuario)
        .grupo(grupo)
        .build();

    seguidoRepository.save(nuevoSeguido);
    return true;
  }

  @Override
  public boolean estaSiguiendo(Long usuarioId, Long grupoId) {
    return seguidoRepository.existsByUsuarioIdAndGrupoId(usuarioId, grupoId);
  }

  @Override
  public long contarSeguidores(Long grupoId) {
    return seguidoRepository.countByGrupoId(grupoId);
  }

  @Override
  public List<SeguidoResponseDTO> listarSeguidos(Long usuarioId) {
    return seguidoRepository.findByUsuarioId(usuarioId)
        .stream()
        .map(seguido -> toResponseDTO(seguido, seguido.getGrupo()))
        .toList();
  }

  @Override
  public List<SeguidoResponseDTO> listarSeguidores(Long grupoId) {
    return seguidoRepository.findByGrupoId(grupoId)
        .stream()
        .map(seguido -> toResponseDTO(seguido, seguido.getUsuario()))
        .toList();
  }

  private SeguidoResponseDTO toResponseDTO(Seguido seguido, Usuario elOtro) {
    String fotoPerfilUrl = (elOtro.getImagenesPerfil() != null && !elOtro.getImagenesPerfil().isEmpty())
        ? elOtro.getImagenesPerfil().get(0).getUrl()
        : null;

    return new SeguidoResponseDTO(
        seguido.getId(),
        elOtro.getId(),
        elOtro.getNombre(),
        elOtro.getRole(),
        fotoPerfilUrl
    );
  }
}
