package lab.arahnik.manager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class Organization {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @OneToOne
  @JoinColumn(name = "address_id")
  private Address address;
  @DecimalMin(value = "0.0")
  private Float annualTurnover;
  @NotNull
  @Min(1)
  private Long employeesCount;
  @NotNull
  @NotBlank
  @Column(unique = true)
  private String fullName;
  @DecimalMin(value = "0.0")
  private Float rating;
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  private User owner;
  @NotNull
  private Boolean editableByAdmin;

}
