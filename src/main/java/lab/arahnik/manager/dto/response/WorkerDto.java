package lab.arahnik.manager.dto.response;

import lab.arahnik.manager.enums.Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkerDto {
    private Long id;
    private String name;
    private Status status;
    private Long ownerId;
}
