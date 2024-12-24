package lab.arahnik.manager.dto.response;

import lab.arahnik.manager.enums.Color;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonDto {
    private Long id;
    private Color eyeColor;
    private Color hairColor;
    private Long locationId;
    private Double height;
    private Long weight;
    private String passportID;
    private Long ownerId;
}
