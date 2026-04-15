package tn.SoftCare.User.controller;

import jakarta.validation.Valid;
import tn.SoftCare.User.dto.CreateUserRequest;
import tn.SoftCare.User.dto.UpdateUserRequest;
import tn.SoftCare.User.dto.UserResponse;
import tn.SoftCare.User.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        return userService.create(req);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> getAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getById(@PathVariable String id) {
        return userService.findById(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse update(@PathVariable String id, @Valid @RequestBody UpdateUserRequest req) {
        return userService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }

    // Ajouter dans UserController.java

    @GetMapping("/by-role/{role}")
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> getByRole(@PathVariable String role) {
        return userService.findByRole(role);
    }
}