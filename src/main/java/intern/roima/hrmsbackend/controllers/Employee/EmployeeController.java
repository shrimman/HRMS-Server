package intern.roima.hrmsbackend.controllers.Employee;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import intern.roima.hrmsbackend.dtos.Requests.UpdateEmployeeDto;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.OrgChartResponseDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Employee_Module.EmployeeDirectoryService;
import intern.roima.hrmsbackend.services.Employee_Module.EmployeeProfileService;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeDirectoryService employeeService;
    private final EmployeeProfileService profileService;

    public EmployeeController(EmployeeDirectoryService employeeService,
            EmployeeProfileService profileService) {
        this.employeeService = employeeService;
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(@CurrentUser Long employeeId) {
        EmployeeSummaryDto profile = profileService.getMyProfile(employeeId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/orgchart/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','HR', 'MANAGER') or #id == authentication.principal.employeeId")
    public ResponseEntity<OrgChartResponseDto> getOrgChart(@PathVariable("id") Long id) {
        return ResponseEntity.ok(employeeService.getOrgChart(id));
    }

    @PostMapping("/profile")
    public ResponseEntity<?> updateCurrentUserProfile(@CurrentUser Long employeeId,
            @RequestBody UpdateEmployeeDto updatedProfile) {
        EmployeeSummaryDto profile = profileService.updateMyProfile(employeeId, updatedProfile);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/department")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<EmployeeSummaryDto>> EmployeesByDepartment(
            @RequestParam("department") String department) {
        return ResponseEntity.ok(employeeService.getEmployeesByDepartment(department));
    }

    @GetMapping("/designation")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<EmployeeSummaryDto>> EmployeesByDesignation(
            @RequestParam("designation") String designation) {
        return ResponseEntity.ok(employeeService.getEmployeesByDesignation(designation));
    }

    @PostMapping("/profile/photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @CurrentUser Long employeeId, @RequestParam("file") MultipartFile file) {
        EmployeeSummaryDto profile = profileService.uploadProfilePhoto(employeeId, file);
        return ResponseEntity.ok(profile);
    }

    @DeleteMapping("/profile/photo")
    public ResponseEntity<?> deleteProfilePhoto(@CurrentUser Long employeeId) {
        EmployeeSummaryDto profile = profileService.deleteProfilePhoto(employeeId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<String>> getAllDepartments() {
        return ResponseEntity.ok(employeeService.getAllDepartments());
    }

    @GetMapping("/designations")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<String>> getAllDesignations() {
        return ResponseEntity.ok(employeeService.getAllDesignations());
    }
}