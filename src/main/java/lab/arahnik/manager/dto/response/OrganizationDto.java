package lab.arahnik.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDto {
    private Long id;
    private String zipCode;
    private Float annualTurnover;
    private Long employeesCount;
    private String fullName;
    private Float rating;
    private Long ownerId;
    private Boolean isEditableByAdmin;
}
