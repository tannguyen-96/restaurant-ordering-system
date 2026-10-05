package com.example.backend.service;

import com.example.backend.dto.request.UserRequest;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserEncodesPasswordAndReturnsSafeResponse() {
        UserRequest request = request("admin", "admin@example.com", "admin", null);
        User savedUser = user("admin", "admin@example.com", "encoded-password", "active");
        when(passwordEncoder.encode("admin")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    void getAllUsersMapsActiveUsers() {
        User user = user("admin", "admin@example.com", "encoded", "active");
        when(userRepository.findAllActiveUsers()).thenReturn(List.of(user));

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).singleElement().satisfies(response -> {
            assertThat(response.getUsername()).isEqualTo("admin");
            assertThat(response.getEmail()).isEqualTo("admin@example.com");
            assertThat(response.getStatus()).isEqualTo("active");
        });
    }

    @Test
    void deleteUserSetsDeleteDate() {
        UUID id = UUID.randomUUID();
        User user = user("admin", "admin@example.com", "encoded", "active");
        user.setId(id);
        when(userRepository.findOneActiveUser(id)).thenReturn(List.of(user));

        userService.deleteUser(id);

        assertThat(user.getDeleteDate()).isNotNull();
        verify(userRepository).save(user);
    }

    private UserRequest request(String username, String email, String password, String status) {
        UserRequest request = new UserRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        request.setStatus(status);
        return request;
    }

    private User user(String username, String email, String passwordHash, String status) {
        LocalDateTime timestamp = LocalDateTime.of(2026, 10, 3, 12, 0);
        return User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .status(status)
                .createdAt(timestamp)
                .updatedAt(timestamp)
                .build();
    }
}
