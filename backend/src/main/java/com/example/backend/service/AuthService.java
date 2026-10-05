package com.example.backend.service;

import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.response.LoginResponse;
import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public LoginResponse authenticate(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return response(null, null, null, null, "Username or password is missing");
        }
        Optional<User> user = userRepository.findByUsernameAndDeleteDateIsNull(request.getUsername());
        if (user.isEmpty()) {
            return response(null, null, null, null, "User not found");
        } else {
            if (!passwordEncoder.matches(request.getPassword(), user.get().getPasswordHash())) {
                return response(null, null, null, null, "Invalid credentials");
            }
            // return response(user.get().getUsername(), roleNames(user.get()), jwtService.generateToken(user.get().getUsername(), roleNames(user.get())),
            //         refreshTokenService.createToken(user.get()), null);
            return response(user.get().getUsername(), roleNames(user.get()), jwtService.generateToken(user.get().getUsername(), roleNames(user.get())),
                    null, null);
        }
            
    }

    public LoginResponse refreshAccessToken(String refreshToken) {
        RefreshTokenService.RotatedRefreshToken rotatedToken = refreshTokenService.rotateToken(refreshToken);
        User user = userRepository.findByUsernameAndDeleteDateIsNull(rotatedToken.username())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        return response(rotatedToken.username(), roleNames(user), jwtService.generateToken(rotatedToken.username(), roleNames(user)),
                rotatedToken.refreshToken(), null);
    }

    private List<String> roleNames(User user) {
        return user.getRoles().stream().map(Role::getName).toList();
    }

    private LoginResponse response(String username, List<String> roleNames, String accessToken, String refreshToken, String message) {
        LoginResponse response = new LoginResponse();
        response.setUsername(username);
        response.setRoleNames(roleNames);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setMessage(message);
        return response;
    }
}