package intern.roima.hrmsbackend.entities.Travel_Module;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "TravelExpenses")
public class TravelExpenses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expenseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TravelId", referencedColumnName = "travelId", nullable = false)
    private TravelPlans travelPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ExpenseTypeId", referencedColumnName = "expenseTypeId", nullable = false)
    private ExpenseType expenseType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = true, length = 255)
    private String hrRemarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ApprovalStatus", referencedColumnName = "statusId", nullable = false)
    private ExpenseStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HR_ActionById", referencedColumnName = "employeeId", nullable = true)
    private Employees hrActionBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedBy", referencedColumnName = "employeeId")
    private Employees updatedByEmployee;

}
