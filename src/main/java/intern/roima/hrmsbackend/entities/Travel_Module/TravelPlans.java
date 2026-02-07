package intern.roima.hrmsbackend.entities.Travel_Module;

import java.time.LocalDateTime;

import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "TravelPlan")
public class TravelPlans {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long travelId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private java.time.LocalDate startDate;

    @Column(nullable = false)
    private java.time.LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedByHRId", referencedColumnName = "employeeId", nullable = false)
    private Employees createdByHR;

    @Column(nullable = false, updatable = false)
    private LocalDateTime CreatedAt;

}
