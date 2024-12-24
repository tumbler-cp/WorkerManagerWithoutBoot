package lab.arahnik.manager.dto.request;

import lab.arahnik.manager.entity.Organization;
import lab.arahnik.manager.enums.Position;
import lab.arahnik.manager.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewWorker {
    private String name;
    private Float x;
    private Float y;
    private Long organizationId;
    private Double salary;
    private Long rating;
    private Position position;
    private Status status;
    private Long personId;
}
