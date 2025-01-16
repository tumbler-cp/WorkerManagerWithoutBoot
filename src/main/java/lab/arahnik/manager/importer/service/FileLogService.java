package lab.arahnik.manager.importer.service;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.dto.UserDto;
import lab.arahnik.authentication.repository.UserRepository;
import lab.arahnik.manager.importer.dto.FileLogResponse;
import lab.arahnik.manager.importer.model.FileLog;
import lab.arahnik.manager.importer.repository.FileLogRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileLogService {

  private final FileLogRepository fileLogRepository;
  private final UserRepository userRepository;

  public List<FileLogResponse> findAll() {
    var list = fileLogRepository.findAll();
    return getFileLogResponses(list);
  }

  public List<FileLogResponse> findByUsername(String username) {
    var list = fileLogRepository.findByOwner_Username(username);
    return getFileLogResponses(list);
  }

  @NotNull
  private List<FileLogResponse> getFileLogResponses(List<FileLog> list) {
    List<FileLogResponse> result = new ArrayList<>();
    for (var fileLog : list) {
      result.add(
              FileLogResponse.builder()
                      .fileName(fileLog.getFileName())
                      .className(fileLog.getClassName())
                      .id(fileLog.getId())
                      .quantity(fileLog.getQuantity())
                      .timestamp(fileLog.getTimestamp()
                              .toString())
                      .owner(UserDto.builder()
                              .id(fileLog.getOwner()
                                      .getId())
                              .username(fileLog.getOwner()
                                      .getUsername())
                              .role(fileLog.getOwner()
                                      .getRole())
                              .build())
                      .build()
      );
    }
    return result;
  }

  public void save(String fileName, int quantity, String classname) {
    var user = userRepository.findByUsername(SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName())
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
    fileLogRepository.save(
            FileLog.builder()
                    .fileName(fileName)
                    .quantity(quantity)
                    .className(classname)
                    .owner(user)
                    .timestamp(LocalDateTime.now())
                    .build()
    );
  }

}
