package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.user.*;
import com.zorvyn.finance.entity.*;
import com.zorvyn.finance.exception.DuplicateResourceException;
import com.zorvyn.finance.exception.ResourceNotFoundException;
import com.zorvyn.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("encoded_password")
                .role(Role.ANALYST)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnUserList() {
        when(userRepository.findAllByDeletedFalse()).thenReturn(List.of(testUser));

        List<UserResponse> users = userService.getAllUsers();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getName()).isEqualTo("John Doe");
        assertThat(users.get(0).getRole()).isEqualTo(Role.ANALYST);
        verify(userRepository).findAllByDeletedFalse();
    }

    @Test
    void getUserById_WhenExists_ShouldReturnUser() {
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getUserById_WhenNotFound_ShouldThrowException() {
        when(userRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void createUser_WithNewEmail_ShouldSucceed() {
        UserRequest request = UserRequest.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .password("password123")
                .role(Role.VIEWER)
                .build();

        when(userRepository.existsByEmailAndDeletedFalse("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(2L);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            return user;
        });

        UserResponse response = userService.createUser(request);

        assertThat(response.getName()).isEqualTo("Jane Doe");
        assertThat(response.getRole()).isEqualTo(Role.VIEWER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        UserRequest request = UserRequest.builder()
                .name("Jane Doe")
                .email("john@example.com")
                .password("password123")
                .role(Role.VIEWER)
                .build();

        when(userRepository.existsByEmailAndDeletedFalse("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void deleteUser_ShouldSoftDelete() {
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deleteUser(1L);

        assertThat(testUser.isDeleted()).isTrue();
        assertThat(testUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserStatus_ShouldUpdateCorrectly() {
        UserStatusRequest request = UserStatusRequest.builder()
                .status(UserStatus.INACTIVE)
                .build();

        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.updateUserStatus(1L, request);

       
        assertThat(testUser.getStatus()).isEqualTo(UserStatus.INACTIVE);

        
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(response.getName()).isEqualTo("John Doe");
        verify(userRepository).save(testUser);
    }
}