package tn.SoftCare.User.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.SoftCare.User.dto.*;
import tn.SoftCare.User.security.AuthenticatedUser;
import tn.SoftCare.User.security.SessionCookieService;
import tn.SoftCare.User.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionCookieService sessionCookieService;

    public AuthController(AuthService authService, SessionCookieService sessionCookieService) {
        this.authService = authService;
        this.sessionCookieService = sessionCookieService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();
        AuthResponse authResponse = authService.login(req, userAgent, ip);
        sessionCookieService.addSessionCookie(response, authResponse.getSessionId());
        return authResponse;
    }

    @PostMapping("/google")
    public AuthResponse googleLogin(@Valid @RequestBody GoogleLoginRequest req,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();
        AuthResponse authResponse = authService.googleLogin(req, userAgent, ip);
        sessionCookieService.addSessionCookie(response, authResponse.getSessionId());
        return authResponse;
    }

    @PostMapping("/facebook")
    public AuthResponse facebookLogin(@Valid @RequestBody FacebookLoginRequest req,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();
        AuthResponse authResponse = authService.facebookLogin(req, userAgent, ip);
        sessionCookieService.addSessionCookie(response, authResponse.getSessionId());
        return authResponse;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        sessionCookieService.extractSessionId(request).ifPresent(authService::logout);
        sessionCookieService.clearSessionCookie(response);
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new org.springframework.security.authentication.BadCredentialsException("Session invalide ou expirée");
        }
        return authService.getCurrentUser(principal.getSessionId());
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return authService.forgotPassword(req);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        return authService.resetPassword(req);
    }
}
