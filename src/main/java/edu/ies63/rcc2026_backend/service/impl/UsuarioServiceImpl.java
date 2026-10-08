package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.ImagenPerfilResponseDTO;
import edu.ies63.rcc2026_backend.dto.PerfilResponseDTO;
import edu.ies63.rcc2026_backend.dto.PerfilUpdateDTO;
import edu.ies63.rcc2026_backend.dto.RedSocialResponseDTO;
import edu.ies63.rcc2026_backend.dto.UsuarioResponseDTO;
import edu.ies63.rcc2026_backend.enums.Plataforma;
import edu.ies63.rcc2026_backend.enums.TipoImagen;
import edu.ies63.rcc2026_backend.exception.PasswordInvalidaException;
import edu.ies63.rcc2026_backend.exception.ProcesamientoImagenException;
import edu.ies63.rcc2026_backend.exception.UsuarioNoEncontradoException;
import edu.ies63.rcc2026_backend.model.ImagenPerfil;
import edu.ies63.rcc2026_backend.model.RedSocial;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.security.dto.CambiarPasswordRequest;
import edu.ies63.rcc2026_backend.service.UsuarioService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UsuarioServiceImpl implements UsuarioService, UserDetailsService {
  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;
  private final String UPLOAD_DIR = "uploads/perfiles/";
  private static final List<String> TIPOS_PERMITIDOS = List.of("image/jpeg", "image/png", "image/webp");

  public UsuarioServiceImpl(UsuarioRepository usuarioRepository, @Lazy PasswordEncoder passwordEncoder) {
    this.usuarioRepository = usuarioRepository;
      this.passwordEncoder = passwordEncoder;
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
  public Page<UsuarioResponseDTO> listarUsuarios(Pageable pageable) {
    return usuarioRepository.findAll(pageable)
        .map(this::toResponseDTO); // Page ya tiene el método .map() integrado, no necesitas .stream()
  }

  // para editar perfil
  @Transactional
  public PerfilResponseDTO actualizarPerfil(String username, PerfilUpdateDTO request) {
    Usuario user = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Usuario no encontrado"
        ));

    // 1. Actualizar datos de texto básicos
    if (request.nombre() != null) user.setNombre(request.nombre());
    if (request.biografia() != null) user.setBiografia(request.biografia());
    if (request.director() != null) user.setDirector(request.director());
    if (request.whatsapp() != null) user.setWhatsapp(request.whatsapp());

    // 2. Actualizar Redes Sociales (llama al método que ya implementaste)
    actualizarRedes(user, request);

    // 3. Actualizar Imágenes
    if (user.getImagenesPerfil() == null) {
      user.setImagenesPerfil(new ArrayList<>());
    }

    if (request.fotoPerfil() != null && !request.fotoPerfil().isEmpty()) {
      // Pasamos el TipoImagen como segundo parámetro
      String url = guardarArchivoLocalmente(request.fotoPerfil(), TipoImagen.FOTOPERFIL);
      actualizarEntidadImagen(user, url, TipoImagen.FOTOPERFIL);
    }

    if (request.fotoBanner() != null && !request.fotoBanner().isEmpty()) {
      // Pasamos el TipoImagen como segundo parámetro
      String url = guardarArchivoLocalmente(request.fotoBanner(), TipoImagen.PORTADA);
      actualizarEntidadImagen(user, url, TipoImagen.PORTADA);
    }

    // 4. Guardar en base de datos
    Usuario usuarioGuardado = usuarioRepository.save(user);

    // 5. Devolver el DTO completo para que el Frontend lo renderice
    return mapearAPerfilResponseDTO(usuarioGuardado);
  }

  // para agregar y actualizar redes sociales
  private void actualizarRedes(Usuario user, PerfilUpdateDTO request) {
    if (user.getRedes() == null) {
      user.setRedes(new ArrayList<>());
    }
    user.getRedes().clear();

    agregarRedSiValida(user, Plataforma.INSTAGRAM, request.instagram());
    agregarRedSiValida(user, Plataforma.FACEBOOK, request.facebook());
    agregarRedSiValida(user, Plataforma.X, request.x());
  }

  private void agregarRedSiValida(Usuario user, Plataforma plataforma, String input) {
    if (input != null && !input.isBlank()) {
      RedSocial red = convertirLinkRedSocial(plataforma, input, user);
      user.getRedes().add(red);
    }
  }

  // para conversion de link de red social
  private RedSocial convertirLinkRedSocial(Plataforma plataforma, String entrada, Usuario usuario) {
    String limpio = entrada.trim()
        .replaceAll("^https?://(www\\.)?", "")
        .replaceAll("/$", "");

    String nombreUsuario;
    String url;

    switch (plataforma) {
      case INSTAGRAM -> {
        nombreUsuario = limpio.replaceFirst("^(instagram\\.com/|@)", "");
        url = "https://www.instagram.com/" + nombreUsuario;
      }
      case FACEBOOK -> {
        nombreUsuario = limpio.replaceFirst("^(facebook\\.com/|@)", "");
        url = "https://www.facebook.com/" + nombreUsuario;
      }
      case X -> {
        nombreUsuario = limpio.replaceFirst("^(x\\.com/|twitter\\.com/|@)", "");
        url = "https://x.com/" + nombreUsuario;
      }
      default -> {
        nombreUsuario = limpio;
        url = entrada.startsWith("http") ? entrada : "https://" + entrada;
      }
    }

    return RedSocial.builder()
        .plataforma(plataforma)
        .nombreUsuario("@" + nombreUsuario)
        .url(url)
        .usuario(usuario)
        .build();
  }

  // para agregar imágenes
  private void actualizarEntidadImagen(Usuario user, String url, TipoImagen tipo) {
    user.getImagenesPerfil().removeIf(img -> {
      if (img.getTipoImagen() == tipo) {
        try {
          Path rutaArchivo = Paths.get(img.getUrl().substring(1));
          Files.deleteIfExists(rutaArchivo);
        } catch (IOException e) {
          log.warn("No se pudo eliminar la imagen anterior: {}", img.getUrl());
        }
        return true;
      }
      return false;
    });

    user.getImagenesPerfil().add(ImagenPerfil.builder()
        .url(url)
        .tipoImagen(tipo)
        .usuario(user)
        .alt("Imagen " + tipo.name() + " de " + user.getNombre())
        .build());
  }

  private String guardarArchivoLocalmente(MultipartFile file, TipoImagen tipo) {
    if (file.getContentType() == null || !TIPOS_PERMITIDOS.contains(file.getContentType())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagen no soportado. Usa JPG, PNG o WEBP.");
    }

    try {
      Path rutaDirectorio = Paths.get(UPLOAD_DIR);
      if (!Files.exists(rutaDirectorio)) {
        Files.createDirectories(rutaDirectorio);
      }

      // EXTRACCIÓN Y LIMPIEZA SEGURA DEL NOMBRE
      String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "imagen";
      originalFilename = Paths.get(originalFilename).getFileName().toString();

      String nombreSinExtension = originalFilename.contains(".")
          ? originalFilename.substring(0, originalFilename.lastIndexOf('.'))
          : originalFilename;
      String nombreLimpio = nombreSinExtension.replaceAll("[^a-zA-Z0-9\\-_]", "_");

      String nombreArchivo = UUID.randomUUID().toString() + "_" + nombreLimpio + ".webp";
      Path rutaArchivo = rutaDirectorio.resolve(nombreArchivo);

      int maxWidth = (tipo == TipoImagen.FOTOPERFIL) ? 400 : 1200;
      int maxHeight = (tipo == TipoImagen.FOTOPERFIL) ? 400 : 600;

      // PROCESAMIENTO CON THUMBNAILATOR
      Thumbnails.of(file.getInputStream())
          .size(maxWidth, maxHeight)
          .outputFormat("webp")
          .outputQuality(0.8)
          .toFile(rutaArchivo.toFile());

      return "/uploads/perfiles/" + nombreArchivo;

    } catch (IOException e) {
      log.error("Error al comprimir/guardar imagen", e);
      throw new ProcesamientoImagenException("No se pudo procesar ni guardar la imagen en el servidor", e);
    }
  }

  public PerfilResponseDTO mapearAPerfilResponseDTO(Usuario u) {
    Long localidadId = u.getLocalidad() != null ? u.getLocalidad().getId() : null;
    String localidadStr = u.getLocalidad() != null
        ? u.getLocalidad().getLocalidad() + ", " + u.getLocalidad().getProvincia()
        : "";

    List<ImagenPerfilResponseDTO> imagenesDto = u.getImagenesPerfil() != null
        ? u.getImagenesPerfil().stream()
        .map(img -> new ImagenPerfilResponseDTO(
            img.getId(), img.getUrl(), img.getAlt(), img.getTipoImagen()
        )).toList()
        : new ArrayList<>();

    List<RedSocialResponseDTO> redesDto = u.getRedes() != null
        ? u.getRedes().stream()
        .map(red -> new RedSocialResponseDTO(
            red.getId(), red.getPlataforma(), red.getUrl(), red.getNombreUsuario()
        )).toList()
        : new ArrayList<>();

    return new PerfilResponseDTO(
        u.getId(), u.getUsername(), u.getRole(), u.getNombre(),
        u.getBiografia(), u.getDirector(),
        localidadId,    // <--- Faltaba pasar este parámetro al constructor
        localidadStr,
        u.getWhatsapp(),
        imagenesDto, redesDto
    );
  }

  @Override
  public PerfilResponseDTO obtenerMiPerfil(String username) {
    Usuario me = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + username));
    return mapearAPerfilResponseDTO(me);
  }

  @Override
  public PerfilResponseDTO verPerfilPublico(Long id) {
    Usuario usuario = usuarioRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: " + id));
    return mapearAPerfilResponseDTO(usuario);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return usuarioRepository.findByUsername(username)
        .orElseThrow(() ->
            new UsernameNotFoundException("Usuario no encontrado: " + username));
  }

  @Override
  @Transactional
  public void cambiarPassword(CambiarPasswordRequest request, Principal connectedUser) {
    String username = connectedUser.getName();

    Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new UsuarioNoEncontradoException("No se encontró el usuario: " + username));

    if (request.newPassword() == null || request.newPassword().length() < 8) {
      throw new PasswordInvalidaException("La nueva contraseña debe tener al menos 8 caracteres");
    }

    if (!passwordEncoder.matches(request.currentPassword(), usuario.getPassword())) {
      throw new PasswordInvalidaException("La contraseña actual es incorrecta");
    }

    if (!request.newPassword().equals(request.confirmationPassword())) {
      throw new PasswordInvalidaException("Las nuevas contraseñas no coinciden");
    }

    if (passwordEncoder.matches(request.newPassword(), usuario.getPassword())) {
      throw new PasswordInvalidaException("La nueva contraseña no puede ser igual a la anterior");
    }

    usuario.setPassword(passwordEncoder.encode(request.newPassword()));
    usuarioRepository.save(usuario);
  }
}
