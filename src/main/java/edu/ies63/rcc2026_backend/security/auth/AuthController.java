package edu.ies63.rcc2026_backend.security.auth;

import edu.ies63.rcc2026_backend.security.dto.AuthRequest;
import edu.ies63.rcc2026_backend.security.dto.AuthResponse;
import edu.ies63.rcc2026_backend.dto.RegisterRequest;
import edu.ies63.rcc2026_backend.security.dto.CambiarPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

  @PostMapping("/register")
  @Operation(summary = "Registrar nuevo usuario")
  public ResponseEntity<AuthResponse> register(
      @Valid @RequestBody RegisterRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(authService.register(request));
  }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PatchMapping("/cambiarpassword")
    @Operation(summary = "Cambiar la contraseña del usuario autenticado")
    public ResponseEntity<Void> cambiarPassword(
            @Valid @RequestBody CambiarPasswordRequest request,
            Principal connectedUser
    ) {
        authService.cambiarPassword(request, connectedUser);
        return ResponseEntity.noContent().build();
    }


}
