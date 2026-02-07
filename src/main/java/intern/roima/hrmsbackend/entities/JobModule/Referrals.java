package intern.roima.hrmsbackend.entities.JobModule;

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
@Table(name = "Referral")
public class Referrals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ReferralId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JobId", referencedColumnName = "JobId", nullable = false)
    private JobOpenings jobOpening;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReferrerId", referencedColumnName = "EmployeeId", nullable = false)
    private Employees referrer;

    @Column(nullable = false, length = 255)
    private String FriendName;

    @Email
    @Column(nullable = false, length = 255)
    private String FriendEmail;

    @Column(nullable = true, length = 255)
    private String CVFilePath;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String Note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReferralStatus", referencedColumnName = "StatusId", nullable = false)
    private ReferralStatus referralStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime CreatedAt;

}
