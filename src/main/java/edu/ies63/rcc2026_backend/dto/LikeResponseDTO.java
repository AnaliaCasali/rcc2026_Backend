package edu.ies63.rcc2026_backend.dto;

public record LikeResponseDTO(
        Long postId,
        boolean liked,
        long totalLikes
) {}