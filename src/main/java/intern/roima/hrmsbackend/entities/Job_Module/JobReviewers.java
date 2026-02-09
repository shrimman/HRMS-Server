package intern.roima.hrmsbackend.entities.Job_Module;

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
@Table(name = "JobReviewers")
public class JobReviewers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JobId", referencedColumnName = "jobId", nullable = false)
    private JobOpenings jobOpening;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeId", referencedColumnName = "employeeId", nullable = false)
    private Employees reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssignedById", referencedColumnName = "employeeId", nullable = false)
    private Employees assignedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedById", referencedColumnName = "employeeId")
    private Employees updatedByEmployee;

}
