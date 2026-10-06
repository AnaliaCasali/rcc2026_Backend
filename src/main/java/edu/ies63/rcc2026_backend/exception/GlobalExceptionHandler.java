package edu.ies63.rcc2026_backend.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // Manejo de errores de validación (@Valid)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación", errors);
  }

  // Manejo de errores 404 (Entidades no encontradas)
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
  }

  // Manejo de errores lanzados por ResponseStatusException (Usado en tu AuthService)
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
    return buildResponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), null);
  }
  @ExceptionHandler(UsuarioNoEncontradoException.class)
  public ResponseEntity<Map<String, Object>> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
  }

  @ExceptionHandler(GrupoNoEncontradoException.class)
  public ResponseEntity<Map<String, Object>> handleGrupoNoEncontrado(GrupoNoEncontradoException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
  }

  @ExceptionHandler(NoEsGrupoException.class)
  public ResponseEntity<Map<String, Object>> handleNoEsGrupo(NoEsGrupoException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
  }

  @ExceptionHandler(AutoSeguimientoException.class)
  public ResponseEntity<Map<String, Object>> handleAutoSeguimiento(AutoSeguimientoException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
  }

  // manejos personalizados
  // Manejo de email duplicado (409)
  @ExceptionHandler(EmailYaRegistradoException.class)
  public ResponseEntity<Map<String, Object>> handleEmailAlreadyRegistered(EmailYaRegistradoException ex) {
    return buildResponse(HttpStatus.CONFLICT, "Conflicto de registro", ex.getMessage());
  }

  // Manejo de contraseñas inválidas (400)
  @ExceptionHandler(PasswordInvalidaException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidPassword(PasswordInvalidaException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación de seguridad", ex.getMessage());
  }

  // Manejo de errores de archivos de imagen (500)
  @ExceptionHandler(ProcesamientoImagenException.class)
  public ResponseEntity<Map<String, Object>> handleImageProcessing(ProcesamientoImagenException ex) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar el archivo multimedia", ex.getMessage());
  }
  // Manejo genérico para cualquier otro error (500)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", ex.getMessage());
  }

  // Método auxiliar para estructurar la respuesta del error
  private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, Object details) {
    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", LocalDateTime.now());
    response.put("status", status.value());
    response.put("error", status.getReasonPhrase());
    response.put("message", message);
    if (details != null) {
      response.put("details", details);
    }
    return new ResponseEntity<>(response, status);
  }

  @ExceptionHandler(BusquedaInvalidaException.class)
  public ResponseEntity<Map<String, Object>> handleBusquedaInvalidaException(BusquedaInvalidaException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put("message", ex.getMessage());
    body.put("timestamp", LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(PostNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handlePostNotFoundException(PostNotFoundException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", HttpStatus.NOT_FOUND.value());
    body.put("error", "Not Found");
    body.put("message", ex.getMessage());
    body.put("timestamp", LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(EventoConflictException.class)
  public ResponseEntity<Map<String, Object>> handleEventoConflict(EventoConflictException ex) {
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("timestamp", LocalDateTime.now());
    response.put("status", HttpStatus.CONFLICT.value());
    response.put("error", HttpStatus.CONFLICT.getReasonPhrase());
    response.put("message", ex.getMessage());
    response.put("details", ex.getDetails());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }
}
