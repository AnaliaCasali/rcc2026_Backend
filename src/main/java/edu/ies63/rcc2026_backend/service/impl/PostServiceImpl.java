package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.ImagenPostDTO;
import edu.ies63.rcc2026_backend.dto.PostCreateDTO;
import edu.ies63.rcc2026_backend.dto.PostResponseDTO;
import edu.ies63.rcc2026_backend.dto.PostUpdateDTO;
import edu.ies63.rcc2026_backend.enums.TipoPost;
import edu.ies63.rcc2026_backend.exception.ProcesamientoImagenException;
import edu.ies63.rcc2026_backend.model.ImagenPost;
import edu.ies63.rcc2026_backend.model.Post;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.PostRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.service.PostService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final UsuarioRepository usuarioRepository;
  private final String UPLOAD_DIR_POSTS = "uploads/posts/";
  private static final List<String> TIPOS_PERMITIDOS = List.of("image/jpeg", "image/png", "image/webp");

  public PostServiceImpl(PostRepository postRepository,
                         UsuarioRepository usuarioRepository) {
    this.postRepository = postRepository;
    this.usuarioRepository = usuarioRepository;
  }

  private PostResponseDTO toResponseDTO(Post post) {
    List<ImagenPostDTO> imagenesDTO = post.getImagenes() != null
        ? post.getImagenes().stream()
        .map(img -> new ImagenPostDTO(img.getId(), img.getUrl()))
        .collect(Collectors.toList())
        : new ArrayList<>();

    Usuario autor = post.getUsuario();
    String fotoPerfilUrl = null;

    if (autor.getImagenesPerfil() != null && !autor.getImagenesPerfil().isEmpty()) {
      fotoPerfilUrl = autor.getImagenesPerfil().getFirst().getUrl();
    }

    return new PostResponseDTO(
        post.getId(),
        autor.getId(),
        autor.getUsername(),
        autor.getNombre(),
        fotoPerfilUrl,

        post.getTitulo(),
        post.getDescripcion(),
        post.getTipoPost(),
        post.getFechaCreacion(),
        post.getLinkVideo(),
        imagenesDTO
    );
  }

  @Override
  public Page<PostResponseDTO> listarPosts(Pageable pageable) {
    return postRepository.findByTipoPost(TipoPost.POST, pageable)
        .map(this::toResponseDTO);
  }

  @Override
  public Page<PostResponseDTO> listarPremios(Pageable pageable) {
    return postRepository.findByTipoPost(TipoPost.PREMIO, pageable)
        .map(this::toResponseDTO);
  }

  @Override
  @Transactional
  public PostResponseDTO crearPost(PostCreateDTO request, String username) {
      Usuario usuario = usuarioRepository.findByUsername(username)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

      if (request.imagenes() == null || request.imagenes().isEmpty() || request.imagenes().stream().allMatch(MultipartFile::isEmpty)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La publicación debe contener al menos una imagen obligatoriamente.");
      }

      Post post = Post.builder()
          .titulo(request.titulo())
          .descripcion(request.descripcion())
          .tipoPost(request.tipoPost())
          .linkVideo(request.linkVideo())
          .usuario(usuario)
          .build();

      // Extraemos las imágenes directamente del DTO
      List<ImagenPost> imagenes = request.imagenes().stream().map(archivo -> {
        String url = guardarArchivoLocalmente(archivo);
        return ImagenPost.builder()
            .url(url)
            .alt("Imagen de " + request.titulo())
            .post(post)
            .build();
      }).collect(Collectors.toList());

      post.setImagenes(imagenes);

      Post postGuardado = postRepository.save(post);
      return toResponseDTO(postGuardado);
  }

  @Override
  @Transactional
  public PostResponseDTO editarPost(Long id, PostUpdateDTO request, String username) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));

    if (!post.getUsuario().getUsername().equals(username)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para editar esta publicación");
    }

    if (request.titulo() != null && !request.titulo().isBlank()) {
      post.setTitulo(request.titulo());
    }
    if (request.descripcion() != null) {
      post.setDescripcion(request.descripcion());
    }
    if (request.linkVideo() != null) {
      post.setLinkVideo(request.linkVideo().isBlank() ? null : request.linkVideo());
    }

    if (request.imagenesAEliminar() != null && !request.imagenesAEliminar().isEmpty()) {
      List<ImagenPost> imagenesParaBorrar = post.getImagenes().stream()
          .filter(img -> request.imagenesAEliminar().contains(img.getId()))
          .toList();

      eliminarImagenesFisicas(imagenesParaBorrar);
      post.getImagenes().removeAll(imagenesParaBorrar);
    }

    if (request.imagenes() != null && !request.imagenes().isEmpty()
        && request.imagenes().stream().noneMatch(MultipartFile::isEmpty)) {

      List<ImagenPost> imagenesAgregadas = request.imagenes().stream().map(archivo -> {
        String url = guardarArchivoLocalmente(archivo);
        return ImagenPost.builder()
            .url(url)
            .alt("Imagen de " + post.getTitulo())
            .post(post) // Vinculamos al post actual
            .build();
      }).toList();

      post.getImagenes().addAll(imagenesAgregadas);
    }

    // 3. Validación de integridad
    if (post.getImagenes().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La publicación debe tener al menos una imagen.");
    }

    Post postActualizado = postRepository.save(post);
    return toResponseDTO(postActualizado);
  }

  // (Método auxiliar añadido en tu PostServiceImpl)
  private void eliminarImagenesFisicas(List<ImagenPost> imagenes) {
    if (imagenes != null) {
      imagenes.forEach(img -> {
        try {
          String rutaRelativa = img.getUrl().startsWith("/") ? img.getUrl().substring(1) : img.getUrl();
          Path rutaArchivo = Paths.get(rutaRelativa);
          Files.deleteIfExists(rutaArchivo);
        } catch (IOException e) {
          log.warn("No se pudo eliminar la imagen física del servidor: {}", img.getUrl());
        }
      });
    }
  }

  @Override
  @Transactional
  public void eliminarPost(Long id, String username) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicación no encontrada"));

    if (!post.getUsuario().getUsername().equals(username)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para eliminar esta publicación");
    }

    if (post.getImagenes() != null) {
      post.getImagenes().forEach(img -> {
        try {
          String rutaRelativa = img.getUrl().startsWith("/") ? img.getUrl().substring(1) : img.getUrl();
          Path rutaArchivo = Paths.get(rutaRelativa);
          Files.deleteIfExists(rutaArchivo);
        } catch (IOException e) {
          log.warn("No se pudo eliminar la imagen física del servidor: {}", img.getUrl());
        }
      });
    }

    postRepository.delete(post);
  }

  // para guardado de imagenes
  private String guardarArchivoLocalmente(MultipartFile file) {
    if (file.getContentType() == null || !TIPOS_PERMITIDOS.contains(file.getContentType())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagen no soportado. Usa JPG, PNG o WEBP.");
    }

    try {
      Path rutaDirectorio = Paths.get(UPLOAD_DIR_POSTS);
      if (!Files.exists(rutaDirectorio)) {
        Files.createDirectories(rutaDirectorio);
      }

      String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "imagen_post";
      originalFilename = Paths.get(originalFilename).getFileName().toString();

      String nombreSinExtension = originalFilename.contains(".")
          ? originalFilename.substring(0, originalFilename.lastIndexOf('.'))
          : originalFilename;
      String nombreLimpio = nombreSinExtension.replaceAll("[^a-zA-Z0-9\\-_]", "_");

      String nombreArchivo = UUID.randomUUID().toString() + "_" + nombreLimpio + ".webp";
      Path rutaArchivo = rutaDirectorio.resolve(nombreArchivo);

      // los posts/premios tienen mayor resolución
      Thumbnails.of(file.getInputStream())
          .size(1920, 1080)
          .outputFormat("webp")
          .outputQuality(0.8)
          .toFile(rutaArchivo.toFile());

      return "/uploads/posts/" + nombreArchivo;

    } catch (IOException e) {
      log.error("Error al comprimir/guardar imagen de post", e);
      throw new ProcesamientoImagenException("No se pudo procesar ni guardar la imagen del post en el servidor", e);
    }
  }
}