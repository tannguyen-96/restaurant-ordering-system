package com.example.backend.service;

import com.example.backend.model.RefreshToken;
import com.example.backend.model.User;
import com.example.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final long REFRESH_TOKEN_DAYS = 30;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public String createToken(User user) {
        return createToken(user, UUID.randomUUID());
    }

    @Transactional
    public RotatedRefreshToken rotateToken(String rawToken) {
        RefreshToken currentToken = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        LocalDateTime now = LocalDateTime.now();
        if (currentToken.getRevokedAt() != null || !currentToken.getExpiresAt().isAfter(now)) {
            throw new RuntimeException("Invalid refresh token");
        }

        currentToken.setRevokedAt(now);
        refreshTokenRepository.save(currentToken);
        String replacement = createToken(currentToken.getUser(), currentToken.getFamilyId());
        return new RotatedRefreshToken(currentToken.getUser().getUsername(), replacement);
    }

    private String createToken(User user, UUID familyId) {
        String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hash(rawToken))
                .familyId(familyId)
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    private String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    public record RotatedRefreshToken(String username, String refreshToken) {
    }
}
