package lab.arahnik.manager.importer.dto;

import lab.arahnik.authentication.dto.UserDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileLogResponse {
  private Long id;
  private String fileName;
  private String className;
  private int quantity;
  private String timestamp;
  private UserDto owner;
}
