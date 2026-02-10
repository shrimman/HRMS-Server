package intern.roima.hrmsbackend.services.Employee_Module;

import java.util.List;

import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.OrgChartResponseDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;

public interface EmployeeDirectoryService {

    List<EmployeeSummaryDto> getAllEmployees();

    OrgChartResponseDto getOrgChart(Long employeeId);

    List<EmployeeSummaryDto> searchEmployees(@CurrentUser Long currentUserId, String query, String department,
            String designation, String role, int page, int size);

    List<EmployeeSummaryDto> getEmployeesByDepartment(String department);

    List<EmployeeSummaryDto> getEmployeesByDesignation(String designation);
}
