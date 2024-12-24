package lab.arahnik.manager.dto.request;

import lab.arahnik.manager.enums.Color;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewPerson {
    private Color eyeColor;
    private Color hairColor;
    private Long locationId;
    private Double height;
    private Long weight;
    private String passportID;
}
