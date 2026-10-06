package edu.ies63.rcc2026_backend.service.impl;

import edu.ies63.rcc2026_backend.dto.ImagenPostDTO;
import edu.ies63.rcc2026_backend.dto.PostCreateDTO;
import edu.ies63.rcc2026_backend.dto.PostResponseDTO;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.enums.TipoImagen;
import edu.ies63.rcc2026_backend.dto.PostUpdateDTO;
import edu.ies63.rcc2026_backend.enums.TipoPost;
import edu.ies63.rcc2026_backend.exception.ProcesamientoImagenException;
import edu.ies63.rcc2026_backend.model.ImagenPerfil;
import edu.ies63.rcc2026_backend.model.ImagenPost;
import edu.ies63.rcc2026_backend.model.Post;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.PostLikeRepository;
import edu.ies63.rcc2026_backend.repository.PostRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.service.PostService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final UsuarioRepository usuarioRepository;
  private final PostLikeRepository likeRepository;
  private final String UPLOAD_DIR_POSTS = "uploads/posts/";
  private static final List<String> TIPOS_PERMITIDOS = List.of("image/jpeg", "image/png", "image/webp");

  public PostServiceImpl(PostRepository postRepository,
                         UsuarioRepository usuarioRepository,
                         PostLikeRepository likeRepository) {
    this.postRepository = postRepository;
    this.usuarioRepository = usuarioRepository;
    this.likeRepository = likeRepository;
  }

  private PostResponseDTO toResponseDTO(Post post, long likesCount, boolean liked) {
    List<ImagenPostDTO> imagenesDTO = post.getImagenes() != null
        ? post.getImagenes().stream()
        .map(img -> new ImagenPostDTO(img.getId(), img.getUrl()))
        .collect(Collectors.toList())
        : new ArrayList<>();

    Usuario autor = post.getUsuario();
    String fotoPerfilUrl = autor.getImagenesPerfil() == null ? null
        : autor.getImagenesPerfil().stream()
        .filter(i -> i.getTipoImagen() == TipoImagen.FOTOPERFIL)
        .map(ImagenPerfil::getUrl)
        .findFirst()
        .orElse(null);

    // el orden tiene que coincidir con PostResponseDTO
    return new PostResponseDTO(
        post.getId(),
        post.getTitulo(),
        post.getDescripcion(),
        post.getTipoPost(),
        post.getLinkVideo(),
        imagenesDTO,
        post.getFechaCreacion(),
        autor.getId(),
        autor.getNombre(),
        fotoPerfilUrl,
        autor.getRole(),
        likesCount,
        liked
    );
  }
  // Para devolver un solo post (por ejemplo al editar): calcula likesCount y liked
  private PostResponseDTO toResponseDTO(Post post, String username) {
    long likes = likeRepository.countByPost_Id(post.getId());
    boolean liked = false;
    if (username != null) {
      liked = usuarioRepository.findByUsername(username)
          .map(u -> likeRepository.existsByPost_IdAndUsuario_Id(post.getId(), u.getId()))
          .orElse(false);
    }
    return toResponseDTO(post, likes, liked);
  }
  // Orden fijo: más reciente primero (ignora cualquier sort que mande el cliente)
  private Pageable masRecientePrimero(Pageable p) {
    return PageRequest.of(p.getPageNumber(), p.getPageSize(),
        Sort.by(Sort.Direction.DESC, "fechaCreacion").and(Sort.by(Sort.Direction.DESC, "id")));
  }
  // Convierte una página de Post a DTO sumando likesCount y liked (2 consultas para toda la página).
  // username == null (visitante) -> liked = false
  private Page<PostResponseDTO> aDto(Page<Post> posts, String username) {
    List<Long> ids = posts.getContent().stream().map(Post::getId).toList();
    if (ids.isEmpty()) {
      return Page.empty(posts.getPageable());
    }

    Map<Long, Long> likes = new HashMap<>();
    likeRepository.contarPorPosts(ids).forEach(fila -> likes.put((Long) fila[0], (Long) fila[1]));

    Set<Long> likeados = new HashSet<>();
    if (username != null) {
      usuarioRepository.findByUsername(username)
          .ifPresent(u -> likeados.addAll(likeRepository.idsConLikeDe(u.getId(), ids)));
    }

    return posts.map(p -> toResponseDTO(p, likes.getOrDefault(p.getId(), 0L), likeados.contains(p.getId())));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostResponseDTO> listarPosts(Pageable pageable, String username) {
    return aDto(postRepository.findByTipoPost(TipoPost.POST, masRecientePrimero(pageable)), username);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostResponseDTO> listarPremios(Pageable pageable, String username) {
    return aDto(postRepository.findByTipoPost(TipoPost.PREMIO, masRecientePrimero(pageable)), username);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostResponseDTO> listarPorUsuario(Long usuarioId, Pageable pageable, String username) {
    return aDto(postRepository.findByUsuarioIdAndTipoPost(usuarioId, TipoPost.POST, masRecientePrimero(pageable)), username);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostResponseDTO> listarFeed(Pageable pageable, String username) {
    Usuario usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    return aDto(postRepository.findFeedDeSeguidos(TipoPost.POST, usuario.getId(), masRecientePrimero(pageable)), username);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostResponseDTO> listarPremiosPorUsuario(Long usuarioId, Pageable pageable, String username) {
    return aDto(postRepository.findByUsuarioIdAndTipoPost(usuarioId, TipoPost.PREMIO, masRecientePrimero(pageable)), username);
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
    return toResponseDTO(postGuardado, 0, false);
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
    return toResponseDTO(postActualizado, username);
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