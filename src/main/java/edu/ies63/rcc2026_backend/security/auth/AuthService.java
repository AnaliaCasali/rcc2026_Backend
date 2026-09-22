package edu.ies63.rcc2026_backend.security.auth;

import edu.ies63.rcc2026_backend.dto.RegisterRequest;
import edu.ies63.rcc2026_backend.enums.Role;
import edu.ies63.rcc2026_backend.model.Localidad;
import edu.ies63.rcc2026_backend.model.Usuario;
import edu.ies63.rcc2026_backend.repository.LocalidadRepository;
import edu.ies63.rcc2026_backend.repository.UsuarioRepository;
import edu.ies63.rcc2026_backend.security.dto.AuthRequest;
import edu.ies63.rcc2026_backend.security.dto.AuthResponse;
import edu.ies63.rcc2026_backend.security.dto.CambiarPasswordRequest;
import edu.ies63.rcc2026_backend.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UsuarioRepository userRepository;
  private final LocalidadRepository localidadRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthResponse register(RegisterRequest request) {
    // 1. Barrera de seguridad contra escalada de privilegios
    if (request.role() == Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No está permitido registrarse como administrador");
    }

    // 2. Validar si el usuario ya existe
    if (userRepository.findByUsername(request.username()).isPresent()) {
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
    userRepository.save(user);

    // 5. Generar token para auto-login inmediato al finalizar el último paso
    Map<String, Object> claims = new HashMap<>();
    claims.put("rol", user.getRole().name());

    String token = jwtService.generateToken(claims, user);
    return new AuthResponse(token);
  }

  public AuthResponse authenticate(AuthRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.username(),
            request.password()
        )
    );

    Usuario user = userRepository.findByUsername(request.username())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Usuario no encontrado: " + request.username()));

    Map<String, Object> claims = new HashMap<>();
    claims.put("rol", user.getRole().name());

    return new AuthResponse(jwtService.generateToken(claims, user));
  }


  @Transactional
  public void cambiarPassword(CambiarPasswordRequest request, Principal connectedUser) {
    Usuario user = userRepository.findByUsername(connectedUser.getName())
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Usuario no encontrado"
        ));

    if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "La contraseña actual no es correcta"
      );
    }

    if (!request.newPassword().equals(request.confirmationPassword())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "La nueva contraseña y su confirmación no coinciden"
      );
    }

    if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "La nueva contraseña no puede ser igual a la actual"
      );
    }

    user.setPassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);
  }

}
