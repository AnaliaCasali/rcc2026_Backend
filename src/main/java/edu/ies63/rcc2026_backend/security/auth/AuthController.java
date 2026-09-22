package edu.ies63.rcc2026_backend.security.auth;

import edu.ies63.rcc2026_backend.dto.*;
import edu.ies63.rcc2026_backend.security.dto.AuthRequest;
import edu.ies63.rcc2026_backend.security.dto.AuthResponse;
import edu.ies63.rcc2026_backend.security.dto.CambiarPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
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

/*
    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody UsuarioCreateDTO request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }*/
    @PostMapping("/register/usuario")
    @Operation(summary = "Registrar usuario estándar")
    public ResponseEntity<AuthResponse> registerUsuario(
            @Valid @RequestBody UsuarioCreateDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerUsuario(request));
    }

    @PostMapping("/register/organizador")
    @Operation(summary = "Registrar organizador")
    public ResponseEntity<AuthResponse> registerOrganizador(
            @Valid @RequestBody OrganizadorCreateDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerOrganizador(request));
    }

    @PostMapping("/register/grupo")
    @Operation(summary = "Registrar grupo")
    public ResponseEntity<AuthResponse> registerGrupo(
            @Valid @RequestBody GrupoCreateDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerGrupo(request));
    }

}
