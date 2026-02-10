package intern.roima.hrmsbackend.services.Employee_Module;

import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;

public interface EmployeeProfileService {
    EmployeeSummaryDto getMyProfile(@CurrentUser Long myEmployeeId);

    public EmployeeSummaryDto updateMyProfile(@CurrentUser Long myEmployeeId, EmployeeSummaryDto updatedProfile);

    public EmployeeSummaryDto updateEmployeeProfile(Long employeeId, EmployeeSummaryDto updatedProfile);

    public EmployeeSummaryDto getEmployeeProfileById(Long employeeId);
}
