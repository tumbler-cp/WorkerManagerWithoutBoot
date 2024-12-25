package lab.arahnik.administration.dto;

import jakarta.persistence.*;
import lab.arahnik.authentication.entity.User;
import lab.arahnik.manager.entity.Worker;
import lab.arahnik.manager.enums.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogDto {
    private Long id;
    private String username;
    private Long userId;
    private String workerName;
    private Long workerId;
    private LocalDateTime time;
    private ChangeType changeType;
}
