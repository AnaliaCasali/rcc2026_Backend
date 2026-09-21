package edu.ies63.rcc2026_backend.security.dto;

public record AuthRequest(
        String username,
        String password) {}
