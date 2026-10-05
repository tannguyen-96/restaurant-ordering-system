package com.example.backend.service;

import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.response.LoginResponse;
import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticateReturnsMissingCredentialsMessageWhenUsernameIsMissing() {
        LoginResponse response = authService.authenticate(loginRequest(null, "password"));

        assertFailureResponse(response, "Username or password is missing");
        verifyNoInteractions(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void authenticateReturnsUserNotFoundMessageWhenUserDoesNotExist() {
        when(userRepository.findByUsernameAndDeleteDateIsNull("admin")).thenReturn(Optional.empty());

        LoginResponse response = authService.authenticate(loginRequest("admin", "password"));

        assertFailureResponse(response, "User not found");
        verify(userRepository).findByUsernameAndDeleteDateIsNull("admin");
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void authenticateReturnsInvalidCredentialsMessageWhenPasswordDoesNotMatch() {
        User user = user("admin", "encoded-password");
        when(userRepository.findByUsernameAndDeleteDateIsNull("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(false);

        LoginResponse response = authService.authenticate(loginRequest("admin", "password"));

        assertFailureResponse(response, "Invalid credentials");
        verify(userRepository).findByUsernameAndDeleteDateIsNull("admin");
        verify(passwordEncoder).matches("password", "encoded-password");
        verifyNoInteractions(jwtService);
    }

    @Test
    void authenticateReturnsTokenAndRolesForValidCredentials() {
        User user = user("admin", "encoded-password");
        when(userRepository.findByUsernameAndDeleteDateIsNull("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken("admin", List.of("ADMIN"))).thenReturn("access-token");

        LoginResponse response = authService.authenticate(loginRequest("admin", "password"));

        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRoleNames()).containsExactly("ADMIN");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        // assertThat(response.getRefreshToken()).isNull();
        assertThat(response.getMessage()).isNull();
        verify(userRepository).findByUsernameAndDeleteDateIsNull("admin");
        verify(passwordEncoder).matches("password", "encoded-password");
        verify(jwtService).generateToken("admin", List.of("ADMIN"));
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtService);
    }

    private LoginRequest loginRequest(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private User user(String username, String passwordHash) {
        return User.builder()
                .username(username)
                .passwordHash(passwordHash)
                .roles(Set.of(Role.builder().name("ADMIN").build()))
                .build();
    }

    private void assertFailureResponse(LoginResponse response, String expectedMessage) {
        assertThat(response.getUsername()).isNull();
        assertThat(response.getRoleNames()).isNull();
        assertThat(response.getAccessToken()).isNull();
        assertThat(response.getRefreshToken()).isNull();
        assertThat(response.getMessage()).isEqualTo(expectedMessage);
    }
}
