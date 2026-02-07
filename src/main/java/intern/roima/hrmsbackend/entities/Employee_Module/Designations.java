package intern.roima.hrmsbackend.entities.Employee_Module;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "Designations")
public class Designations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long designationId;

    @Column(nullable = false)
    private String designationName;

    @Column(nullable = true)
    private String description;

}
