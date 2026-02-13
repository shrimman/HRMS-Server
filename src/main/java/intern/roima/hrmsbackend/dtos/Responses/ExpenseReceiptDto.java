package intern.roima.hrmsbackend.dtos.Responses;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseReceiptDto {

    private Long expenseReceiptId;
    private String fileName;
    private String receiptPath;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    private EmployeeSummaryDto updatedByEmployee;

}
