package lab.arahnik.authentication.service;

import lab.arahnik.authentication.dto.AuthRequest;
import lab.arahnik.authentication.dto.AuthToken;
import lab.arahnik.authentication.entity.User;
import lab.arahnik.authentication.enums.Role;
import lab.arahnik.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  public AuthToken register(AuthRequest request) {
    var user = User
            .builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER)
            .build();
    userRepository.save(user);
    var token = jwtService.generateToken(user);
    return AuthToken
            .builder()
            .token(token)
            .build();
  }

  public AuthToken authenticate(AuthRequest request) {
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
            )
    );
    var user = userRepository
            .findByUsername(request.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException(request.getUsername()));
    var token = jwtService.generateToken(user);
    return AuthToken
            .builder()
            .token(token)
            .build();
  }

}
