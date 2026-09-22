package edu.ies63.rcc2026_backend.security.auth;

import edu.ies63.rcc2026_backend.security.dto.AuthResponse;
import edu.ies63.rcc2026_backend.security.jwt.JwtService;
import edu.ies63.rcc2026_backend.dto.RegisterRequest;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Localidad;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.LocalidadRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UsuarioRepository usuarioRepository;
  private final LocalidadRepository localidadRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthResponse register(RegisterRequest request) {
    // 1. Barrera de seguridad contra escalada de privilegios
    if (request.role() == Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No está permitido registrarse como administrador");
    }

    // 2. Validar si el usuario ya existe
    if (usuarioRepository.findByUsername(request.username()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe: " + request.username());
    }

    // 3. Constructor base del usuario común
    Usuario.UsuarioBuilder userBuilder = Usuario.builder()
        .username(request.username())
        .password(passwordEncoder.encode(request.password()))
        .nombre(request.nombre())
        .biografia(request.biografia())
        .role(request.role())
        .debaja(false);

    // 4. Lógica por condicional según el rol elegido
    switch (request.role()) {
      case ORGANIZADOR, GRUPO -> {
        if (request.localidadId() == null) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La localidad es obligatoria para este tipo de cuenta");
        }
        Localidad localidad = localidadRepository.findById(request.localidadId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Localidad no encontrada"));

        userBuilder.director(request.director());
        userBuilder.localidad(localidad);
      }
      case USER -> {
        // El usuario común no requiere localidad ni director obligatorio
      }
      default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no válido");
    }

    Usuario user = userBuilder.build();
    usuarioRepository.save(user);

    // 5. Generar token para auto-login inmediato al finalizar el último paso
    Map<String, Object> claims = new HashMap<>();
    claims.put("rol", user.getRole().name());

    String token = jwtService.generateToken(claims, user);
    return new AuthResponse(token);
  }
}
