package intern.roima.hrmsbackend.dtos.Responses;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TravelExpenseDto {
    private Long expenseId;
    private Long travelPlanId;
    private String travelPlanTitle;
    private Long expenseTypeId;
    private String expenseTypeName;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
    private String hrRemarks;
    private Long approvalStatusId;
    private String approvalStatusName;
    private EmployeeSummaryDto hrActionBy;
    private EmployeeSummaryDto submittedBy;
    private List<ExpenseReceiptDto> receipts;
    private List<EmployeeSummaryDto> participants;
}
