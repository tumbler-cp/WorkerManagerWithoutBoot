package lab.arahnik.manager.controller;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.enums.Role;
import lab.arahnik.authentication.repository.UserRepository;
import lab.arahnik.manager.importer.dto.FileLogResponse;
import lab.arahnik.manager.importer.service.FileLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/file_log")
@RequiredArgsConstructor
public class FileLogController {

  private final FileLogService fileLogService;
  private final UserRepository userRepository;

  @GetMapping("/mine")
  public List<FileLogResponse> getFileLogs() {
    var user = userRepository.findByUsername(
                    SecurityContextHolder.getContext()
                            .getAuthentication()
                            .getName()
            )
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
    if (user.getRole() == Role.ADMIN) {
      return fileLogService.findAll();
    }
    return fileLogService.findByUsername(user.getUsername());
  }

}
