package lab.arahnik.authentication.controller;

import lab.arahnik.authentication.dto.AuthRequest;
import lab.arahnik.authentication.dto.AuthToken;
import lab.arahnik.authentication.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(originPatterns = "*")
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationService service;

  @PostMapping("/register")
  public ResponseEntity<AuthToken> register(
          @RequestBody AuthRequest authRequest
  ) {
    return ResponseEntity.ok(service.register(authRequest));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthToken> authenticate(
          @RequestBody AuthRequest authRequest
  ) {
    return ResponseEntity.ok(service.authenticate(authRequest));
  }

}
