package lab.arahnik.manager.controller;

import jakarta.persistence.EntityNotFoundException;
import lab.arahnik.authentication.enums.Role;
import lab.arahnik.authentication.repository.UserRepository;
import lab.arahnik.manager.importer.dto.FileLogResponse;
import lab.arahnik.manager.importer.service.FileLogService;
import lab.arahnik.minio.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/file")
@PropertySource("classpath:application.properties")
@RequiredArgsConstructor
public class FileController {

  @Value("${minio.bucket-name}")
  private String bucket;

  private final FileLogService fileLogService;
  private final UserRepository userRepository;
  private final MinioService minioService;

  @GetMapping("/logs")
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

  @GetMapping("/download/{fileName}")
  public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String fileName) {
    try {
      String bucketName = "filebucket";
      InputStream fileStream = minioService.getFile(bucketName, fileName);

      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
      headers.setContentType(MediaType.parseMediaType("text/csv"));

      return ResponseEntity.ok()
              .headers(headers)
              .body(new InputStreamResource(fileStream));

    } catch (Exception e) {
      System.out.println(e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(null);
    }
  }

}
