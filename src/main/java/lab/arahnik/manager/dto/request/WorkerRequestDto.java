package lab.arahnik.manager.dto.request;

import lab.arahnik.manager.enums.Status;
import lombok.Data;

@Data
public class WorkerRequestDto {
    private String name;
    private Status status;
}
