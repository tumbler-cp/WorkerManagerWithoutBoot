package lab.arahnik.administration.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lab.arahnik.authentication.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @OneToOne
  @JoinColumn(name = "user_id", unique = true)
  private User user;
  @NotNull
  private AdminRequestStatus status;

}
