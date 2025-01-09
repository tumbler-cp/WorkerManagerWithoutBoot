package lab.arahnik.config;

import lab.arahnik.authentication.entity.User;
import lab.arahnik.authentication.enums.Role;
import lab.arahnik.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationListener<ContextRefreshedEvent> {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.admin.username}")
  private String adminUsername;
  @Value("${app.admin.password}")
  private String adminPassword;

  @Override
  public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
    if (userRepository
            .findByRole(Role.ADMIN)
            .isPresent()) {
      return;
    }
    User user = User
            .builder()
            .username(adminUsername)
            .password(passwordEncoder.encode(adminPassword))
            .role(Role.ADMIN)
            .build();
    userRepository.save(user);
  }

}
