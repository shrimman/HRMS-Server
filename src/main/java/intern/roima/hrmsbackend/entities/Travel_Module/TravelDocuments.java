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
@Table(name = "TravelDocuments")
public class TravelDocuments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long DocumentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TravelId", referencedColumnName = "TravelId", nullable = false)
    private TravelPlans travelPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeId", referencedColumnName = "EmployeeId", nullable = false)
    private Employees employee;

    @Column(nullable = false, length = 100)
    private String DocumentType;

    @Column(nullable = false, length = 255)
    private String DocumentName;

    @Column(nullable = false, length = 255)
    private String DocumentPath;

    @Column(nullable = false, updatable = false)
    private LocalDateTime UploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UploadedById", referencedColumnName = "EmployeeId", nullable = false)
    private Employees uploadedBy;

}
