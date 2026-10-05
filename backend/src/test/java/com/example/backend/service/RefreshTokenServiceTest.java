package com.example.backend.service;

import com.example.backend.model.RefreshToken;
import com.example.backend.model.User;
import com.example.backend.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void createTokenStoresHashAndReturnsRawToken() {
        User user = User.builder().username("admin").build();
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String rawToken = refreshTokenService.createToken(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken savedToken = captor.getValue();
        assertThat(rawToken).isNotBlank();
        assertThat(savedToken.getTokenHash()).isNotEqualTo(rawToken).hasSize(64);
        assertThat(savedToken.getUser()).isSameAs(user);
        assertThat(savedToken.getFamilyId()).isNotNull();
        assertThat(savedToken.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void rotateTokenRejectsUnknownToken() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.rotateToken("unknown-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid refresh token");
    }
}