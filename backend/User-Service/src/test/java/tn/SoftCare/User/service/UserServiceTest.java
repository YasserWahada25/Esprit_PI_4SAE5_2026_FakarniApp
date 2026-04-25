package tn.SoftCare.User.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.SoftCare.User.dto.CreateUserRequest;
import tn.SoftCare.User.dto.UpdateUserRequest;
import tn.SoftCare.User.dto.UserResponse;
import tn.SoftCare.User.exception.EmailAlreadyUsedException;
import tn.SoftCare.User.model.Role;
import tn.SoftCare.User.model.User;
import tn.SoftCare.User.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
    void createShouldEncodePasswordAndPersistUser() {
        CreateUserRequest request = buildCreateRequest();
        User savedUser = buildUser();

        when(userRepository.existsByEmail("sara@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse result = userService.create(request);

        assertThat(result.getEmail()).isEqualTo("sara@example.com");
        assertThat(result.getRole()).isEqualTo(Role.PATIENT_PROFILE);
        verify(passwordEncoder).encode("Password1!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createShouldRejectDuplicateEmail() {
        CreateUserRequest request = buildCreateRequest();
        when(userRepository.existsByEmail("sara@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessage("Email déjà utilisé");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateShouldChangeEmailWhenAvailable() {
        User existingUser = buildUser();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@example.com");
        request.setAdresse("Sousse");

        when(userRepository.findById("user-1")).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserResponse result = userService.update("user-1", request);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getAdresse()).isEqualTo("Sousse");
    }

    @Test
    void findAllShouldMapEntitiesToResponses() {
        when(userRepository.findAll()).thenReturn(List.of(buildUser()));

        List<UserResponse> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getEmail()).isEqualTo("sara@example.com");
    }

    private static CreateUserRequest buildCreateRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setNom("Sara");
        request.setPrenom("Ben Ali");
        request.setEmail("sara@example.com");
        request.setPassword("Password1!");
        request.setRole(Role.PATIENT_PROFILE);
        request.setNumTel("12345678");
        request.setAdresse("Tunis");
        return request;
    }

    private static User buildUser() {
        User user = new User();
        user.setId("user-1");
        user.setNom("Sara");
        user.setPrenom("Ben Ali");
        user.setEmail("sara@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.PATIENT_PROFILE);
        user.setNumTel("12345678");
        user.setAdresse("Tunis");
        return user;
    }
}
