package intern.roima.hrmsbackend.services.Employee_Module;

import org.springframework.web.multipart.MultipartFile;

import intern.roima.hrmsbackend.dtos.Requests.UpdateEmployeeDto;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;

public interface EmployeeProfileService {
    EmployeeSummaryDto getMyProfile(@CurrentUser Long myEmployeeId);

    public EmployeeSummaryDto updateMyProfile(@CurrentUser Long myEmployeeId, UpdateEmployeeDto updatedProfile);

    public EmployeeSummaryDto updateEmployeeProfile(Long employeeId, EmployeeSummaryDto updatedProfile);

    public EmployeeSummaryDto getEmployeeProfileById(Long employeeId);

    public EmployeeSummaryDto uploadProfilePhoto(Long employeeId, MultipartFile file);

    public EmployeeSummaryDto deleteProfilePhoto(Long employeeId);
}
