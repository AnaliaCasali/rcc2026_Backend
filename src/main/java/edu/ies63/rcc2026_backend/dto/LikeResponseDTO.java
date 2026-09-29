package edu.ies63.rcc2026_backend.dto;

public record LikeResponseDTO(
        boolean liked,
        long totalLikes
) {}