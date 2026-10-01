package edu.ies63.rcc2026_backend.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(String message) {
        super(message);
    }
    public PostNotFoundException(Long id) {super("Publicación no encontrada con ID: " + id);}
}
