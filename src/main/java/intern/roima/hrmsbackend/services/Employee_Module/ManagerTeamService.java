package intern.roima.hrmsbackend.services.Employee_Module;

import java.util.List;

import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;

public interface ManagerTeamService {
    List<EmployeeSummaryDto> getMyTeam(@CurrentUser Long managerId);
}
