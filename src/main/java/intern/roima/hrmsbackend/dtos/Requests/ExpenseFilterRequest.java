package intern.roima.hrmsbackend.dtos.Requests;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseFilterRequest {

    private Long employeeId;

    private Long travelPlanId;

    private Long statusId;

    private LocalDate startDate;

    private LocalDate endDate;

    private Long expenseTypeId;
}
