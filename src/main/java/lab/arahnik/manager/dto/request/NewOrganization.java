package lab.arahnik.manager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewOrganization {

  private String zipCode;
  private Float annualTurnover;
  private Long employeesCount;
  private String fullName;
  private Float rating;
  private Boolean editableByAdmin;

}
