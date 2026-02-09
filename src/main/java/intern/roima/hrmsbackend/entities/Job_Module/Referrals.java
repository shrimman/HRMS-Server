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
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Referrals")
public class Referrals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long referralId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JobId", referencedColumnName = "jobId", nullable = false)
    private JobOpenings jobOpening;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReferrerId", referencedColumnName = "employeeId", nullable = false)
    private Employees referrer;

    @Column(nullable = false, length = 255)
    private String friendName;

    @Email
    @Column(nullable = false, length = 255)
    private String friendEmail;

    @Column(nullable = true, length = 255)
    private String cvFilePath;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReferralStatus", referencedColumnName = "statusId", nullable = false)
    private ReferralStatus referralStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedById", referencedColumnName = "employeeId")
    private Employees updatedByEmployee;

}
