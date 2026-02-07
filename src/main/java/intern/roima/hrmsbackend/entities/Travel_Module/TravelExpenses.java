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
    private Long ExpenseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TravelId", referencedColumnName = "TravelId", nullable = false)
    private TravelPlans travelPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeId", referencedColumnName = "EmployeeId", nullable = false)
    private Employees employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ExpenseReceiptId", referencedColumnName = "ExpenseReceiptId", nullable = true)
    private ExpenseReceipt expenseReceipt;

    @Column(nullable = false, length = 100)
    private String ExpenseType;

    @Column(nullable = false)
    private BigDecimal Amount;

    @Column(nullable = false)
    private LocalDate ExpenseDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime SubmittedAt;

    @Column(nullable = true)
    private LocalDateTime UpdatedAt;

    @Column(nullable = true, length = 255)
    private String HR_Remarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ApprovalStatus", referencedColumnName = "StatusId", nullable = false)
    private ExpenseStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "HR_ActionById", referencedColumnName = "EmployeeId", nullable = true)
    private Employees hrActionBy;

}
