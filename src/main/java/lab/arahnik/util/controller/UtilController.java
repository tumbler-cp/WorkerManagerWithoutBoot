package lab.arahnik.util.controller;

import lab.arahnik.authentication.dto.UserDto;
import lab.arahnik.authentication.entity.User;
import lab.arahnik.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/util")
@RequiredArgsConstructor
public class UtilController {
    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserDto me() {
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserDto
                .builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

}
