package tn.SoftCare.User.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.SoftCare.User.dto.CreateUserRequest;
import tn.SoftCare.User.dto.UserResponse;
import tn.SoftCare.User.exception.EmailAlreadyUsedException;
import tn.SoftCare.User.exception.GlobalExceptionHandler;
import tn.SoftCare.User.model.Role;
import tn.SoftCare.User.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createShouldReturnCreatedUser() throws Exception {
        when(userService.create(any(CreateUserRequest.class))).thenReturn(buildUserResponse());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": "Sara",
                                  "prenom": "Ben Ali",
                                  "email": "sara@example.com",
                                  "password": "Password1!",
                                  "role": "PATIENT_PROFILE",
                                  "numTel": "12345678",
                                  "adresse": "Tunis"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("sara@example.com"))
                .andExpect(jsonPath("$.role").value("PATIENT_PROFILE"));
    }

    @Test
    void createShouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        when(userService.create(any(CreateUserRequest.class))).thenThrow(new EmailAlreadyUsedException());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nom": "Sara",
                                  "prenom": "Ben Ali",
                                  "email": "sara@example.com",
                                  "password": "Password1!",
                                  "role": "PATIENT_PROFILE",
                                  "numTel": "12345678",
                                  "adresse": "Tunis"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("This email is already registered. Sign in or use another email."));
    }

    @Test
    void getAllShouldReturnUsers() throws Exception {
        when(userService.findAll()).thenReturn(List.of(buildUserResponse()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("sara@example.com"));
    }

    private static UserResponse buildUserResponse() {
        UserResponse user = new UserResponse();
        user.setId("user-1");
        user.setNom("Sara");
        user.setPrenom("Ben Ali");
        user.setEmail("sara@example.com");
        user.setRole(Role.PATIENT_PROFILE);
        user.setNumTel("12345678");
        user.setAdresse("Tunis");
        return user;
    }
}
