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
@Table(name = "JobShareLog")
public class JobShareLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shareId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JobId", referencedColumnName = "jobId", nullable = false)
    private JobOpenings jobOpening;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SharedById", referencedColumnName = "employeeId", nullable = false)
    private Employees sharedBy;

    @Column(nullable = false, length = 255)
    private String recipientEmail;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sharedAt;

}
