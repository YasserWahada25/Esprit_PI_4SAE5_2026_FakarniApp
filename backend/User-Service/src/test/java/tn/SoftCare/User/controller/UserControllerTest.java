package tn.SoftCare.User.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.SoftCare.User.dto.CreateUserRequest;
import tn.SoftCare.User.dto.UserResponse;
import tn.SoftCare.User.exception.EmailAlreadyUsedException;
import tn.SoftCare.User.model.Role;
import tn.SoftCare.User.security.SessionAuthenticationFilter;
import tn.SoftCare.User.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SessionAuthenticationFilter sessionAuthenticationFilter;

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
                .andExpect(jsonPath("$.message").value("Email déjà utilisé"));
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
