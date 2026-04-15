package tn.SoftCare.User.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.SoftCare.User.dto.*;
import tn.SoftCare.User.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();
        return authService.login(req, userAgent, ip);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req.getRefreshToken());
    }

    // ✅ NEW: forgot password (send email with token)
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req.getEmail());
        // ✅ Security: always return same message
        return new MessageResponse("If the email exists, a reset link has been sent.");
    }

    // ✅ NEW: reset password (token + new password)
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.getToken(), req.getNewPassword());
        return new MessageResponse("Password has been reset successfully.");
    }
}