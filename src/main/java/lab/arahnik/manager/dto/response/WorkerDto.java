package lab.arahnik.manager.dto.response;

import lab.arahnik.manager.entity.Coordinates;
import lab.arahnik.manager.enums.Position;
import lab.arahnik.manager.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerDto {

  private Long id;
  private String name;
  private Coordinates coordinates;
  private LocalDate creationDate;
  private Long organizationId;
  private Double salary;
  private Long rating;
  private Position position;
  private Status status;
  private Long personId;
  private Long ownerId;
  private Boolean isEditableByAdmin;

}
