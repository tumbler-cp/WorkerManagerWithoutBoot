package lab.arahnik.administration.entity;

import jakarta.persistence.*;
import lab.arahnik.authentication.entity.User;
import lab.arahnik.manager.entity.Worker;
import lab.arahnik.manager.enums.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Log {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;
  @ManyToOne(fetch = FetchType.LAZY)
  private Worker worker;
  private LocalDateTime time;
  @Enumerated(EnumType.STRING)
  private ChangeType changeType;

}
