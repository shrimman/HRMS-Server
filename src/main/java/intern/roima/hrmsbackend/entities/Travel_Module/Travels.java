package intern.roima.hrmsbackend.entities.Travel_Module;

import java.io.Serializable;

import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "Travels")
@IdClass(TravelId.class)
public class Travels {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TravelId", referencedColumnName = "travelId", nullable = false)
    private TravelPlans travelPlan;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeId", referencedColumnName = "employeeId", nullable = false)
    private Employees employee;

}

class TravelId implements Serializable {
    private Long travelPlan;
    private Long employee;
}
