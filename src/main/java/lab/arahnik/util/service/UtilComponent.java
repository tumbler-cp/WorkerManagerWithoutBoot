package lab.arahnik.util.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class UtilComponent {

  public String getTmpFilePath(MultipartFile file) {
    return System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename();
  }

  public void transfer(MultipartFile file, String filePath) throws IOException {
    file.transferTo(new File(filePath));
  }

}
