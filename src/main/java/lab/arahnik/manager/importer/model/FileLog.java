package lab.arahnik.manager.importer.model;

import jakarta.persistence.*;
import lab.arahnik.authentication.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String fileName;
  private String className;
  private int quantity;
  private LocalDateTime timestamp;

  @ManyToOne
  private User owner;

}
