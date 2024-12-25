package lab.arahnik.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {
    private Long id;
    private Double x;
    private Long y;
    private String name;
    private Long ownerId;
    private Boolean isEditableByAdmin;
}
