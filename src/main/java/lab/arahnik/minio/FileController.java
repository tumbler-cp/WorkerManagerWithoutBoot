package lab.arahnik.minio;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            fileService.uploadFile(file.getOriginalFilename(), inputStream, file.getContentType());
            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("File upload failed");
        }
    }

    @GetMapping("/download/{objectName}")
    public ResponseEntity<InputStream> downloadFile(@PathVariable String objectName) {
        try {
            InputStream file = fileService.downloadFile(objectName);
            return ResponseEntity.ok().body(file);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }


}
