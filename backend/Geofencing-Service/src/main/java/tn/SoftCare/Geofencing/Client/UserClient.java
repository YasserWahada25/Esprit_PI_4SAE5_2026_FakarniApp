package tn.SoftCare.Geofencing.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import tn.SoftCare.Geofencing.dto.UserDto;

import java.util.List;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable("id") String id);

    @GetMapping("/api/users")
    List<UserDto> getAllUsers();

    @GetMapping("/api/users/by-role")
    List<UserDto> getUsersByRole(@RequestParam("role") String role);
}
