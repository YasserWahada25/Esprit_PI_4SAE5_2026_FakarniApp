package tn.SoftCare.User.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.SoftCare.User.dto.AuthResponse;
import tn.SoftCare.User.dto.LoginRequest;
import tn.SoftCare.User.dto.UserResponse;
import tn.SoftCare.User.model.Role;
import tn.SoftCare.User.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    @Test
    void loginShouldReturnUserAndTokens() throws Exception {
        AuthResponse response = new AuthResponse();
        response.setAccessToken("access-token-123");
        response.setRefreshToken("refresh-token-123");
        response.setUser(buildUserResponse());

        when(authService.login(any(LoginRequest.class), any(String.class), any(String.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "JUnit")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .content("""
                                {
                                  "email": "sara@example.com",
                                  "password": "Password1!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("sara@example.com"))
                .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-123"));
    }

    @Test
    void logoutShouldRevokeRefreshToken() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token-123"
                                }
                                """))
                .andExpect(status().isNoContent());

        verify(authService).logout("refresh-token-123");
    }
    private static UserResponse buildUserResponse() {
        UserResponse user = new UserResponse();
        user.setId("user-1");
        user.setEmail("sara@example.com");
        user.setNom("Sara");
        user.setPrenom("Ben Ali");
        user.setRole(Role.ADMIN);
        user.setAdresse("Tunis");
        user.setNumTel("12345678");
        return user;
    }
}
