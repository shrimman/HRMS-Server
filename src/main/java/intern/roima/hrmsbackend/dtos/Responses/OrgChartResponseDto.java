package intern.roima.hrmsbackend.dtos.Responses;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrgChartResponseDto {
    private EmployeeSummaryDto selectedEmployee;
    private List<EmployeeSummaryDto> managerChain;
    private List<EmployeeSummaryDto> directReports;
}
