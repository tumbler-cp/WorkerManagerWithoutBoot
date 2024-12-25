package lab.arahnik.administration.dto;

import lab.arahnik.administration.entity.AdminRequestStatus;
import lab.arahnik.authentication.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminRequestElem {
    private Long id;
    private Long userId;
    private AdminRequestStatus status;
}
