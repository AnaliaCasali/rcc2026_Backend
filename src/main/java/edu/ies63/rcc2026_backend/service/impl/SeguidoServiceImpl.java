package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Seguido;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.SeguidoRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.service.SeguidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeguidoServiceImpl implements SeguidoService {

  private final SeguidoRepository seguidoRepository;
  private final UsuarioRepository usuarioRepository;

  @Override
  public boolean toggleSeguimiento(Long usuarioId, Long grupoId) {

    // No puede seguirse a sí mismo
    if (usuarioId.equals(grupoId)) {
      throw new IllegalArgumentException(
          "Un usuario no puede seguirse a sí mismo"
      );
    }

    // Buscamos al usuario que quiere seguir
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() ->
            new RuntimeException("Usuario no encontrado")
        );

    // Buscamos el grupo
    Usuario grupo = usuarioRepository.findById(grupoId)
        .orElseThrow(() ->
            new RuntimeException("Grupo no encontrado")
        );

    // Verificamos que realmente sea un grupo
    if (grupo.getRole() != Role.GRUPO) {
      throw new IllegalArgumentException(
          "El usuario indicado no es una agrupación"
      );
    }

    // ¿Ya existe el seguimiento?
    var seguido = seguidoRepository
        .findByUsuarioIdAndGrupoId(usuarioId, grupoId);

    if (seguido.isPresent()) {

      // Ya lo seguía → dejar de seguir
      seguidoRepository.delete(seguido.get());

      return false;
    }

    // No lo seguía → crear seguimiento
    Seguido nuevoSeguido = Seguido.builder()
        .usuario(usuario)
        .grupo(grupo)
        .build();

    seguidoRepository.save(nuevoSeguido);

    return true;
  }

  @Override
  public boolean estaSiguiendo(Long usuarioId, Long grupoId) {

    return seguidoRepository.existsByUsuarioIdAndGrupoId(
        usuarioId,
        grupoId
    );
  }

  @Override
  public long contarSeguidores(Long grupoId) {

    return seguidoRepository.countByGrupoId(grupoId);
  }
}