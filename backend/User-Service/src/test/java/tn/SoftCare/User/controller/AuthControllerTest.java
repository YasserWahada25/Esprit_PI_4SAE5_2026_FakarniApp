package tn.SoftCare.User.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tn.SoftCare.User.dto.AuthResponse;
import tn.SoftCare.User.dto.LoginRequest;
import tn.SoftCare.User.dto.UserResponse;
import tn.SoftCare.User.model.Role;
import tn.SoftCare.User.security.SessionAuthenticationFilter;
import tn.SoftCare.User.security.SessionCookieService;
import tn.SoftCare.User.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SessionCookieService.class)
@TestPropertySource(properties = {
        "app.security.cookie.secure=false",
        "app.security.cookie.same-site=Lax",
        "app.security.session-days=7"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private SessionAuthenticationFilter sessionAuthenticationFilter;

    @Test
    void loginShouldReturnUserAndSetSessionCookie() throws Exception {
        AuthResponse response = new AuthResponse();
        response.setSessionId("session-123");
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
                .andExpect(cookie().value(SessionCookieService.SESSION_COOKIE_NAME, "session-123"))
                .andExpect(cookie().httpOnly(SessionCookieService.SESSION_COOKIE_NAME, true));
    }

    @Test
    void logoutShouldRevokeSessionAndClearCookie() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie(SessionCookieService.SESSION_COOKIE_NAME, "session-123")))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(SessionCookieService.SESSION_COOKIE_NAME, 0));

        verify(authService).logout("session-123");
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
