package lab.arahnik.manager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lab.arahnik.authentication.entity.User;
import lab.arahnik.manager.enums.Position;
import lab.arahnik.manager.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @NotBlank
    private String name;
    @OneToOne
    @JoinColumn(name = "coordinates_id")
    private Coordinates coordinates;
    @NotNull
    private LocalDate creationDate;
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
    @DecimalMin(value = "0.0")
    private Double salary;
    @Min(1)
    private Long rating;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Position position;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;
    @NotNull
    @OneToOne
    @JoinColumn(name = "person_id", unique = true)
    private Person person;
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private User owner;
    @NotNull
    private Boolean editableByAdmin;

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDate.now();
    }
}
