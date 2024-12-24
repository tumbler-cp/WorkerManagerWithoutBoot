package lab.arahnik.authentication.service;

import lab.arahnik.authentication.entity.User;
import lab.arahnik.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    public Long getCurrentUserId() {
        return getByUsername(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName())
                .getId();
    }
}
