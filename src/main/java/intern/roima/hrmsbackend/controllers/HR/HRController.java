package intern.roima.hrmsbackend.controllers.HR;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import intern.roima.hrmsbackend.dtos.Requests.UpdateEmployeeProfileDto;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Employee_Module.EmployeeDirectoryService;
import intern.roima.hrmsbackend.services.Employee_Module.EmployeeProfileService;

@RestController
@RequestMapping("/api/hr")
public class HRController {

    private final EmployeeDirectoryService employeeService;
    private final EmployeeProfileService profileService;

    public HRController(EmployeeDirectoryService employeeService,
            EmployeeProfileService profileService) {
        this.employeeService = employeeService;
        this.profileService = profileService;
    }

    @GetMapping("/allemployees")
    @PreAuthorize("hasAnyRole('HR','MANAGER','EMPLOYEE')")
    public ResponseEntity<?> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE') or #id == authentication.principal.employeeId")
    public ResponseEntity<EmployeeSummaryDto> getEmployeeById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(profileService.getEmployeeProfileById(id));
    }

    @GetMapping("/searchEmployee")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER','EMPLOYEE')")
    public ResponseEntity<?> searchEmployees(
            @CurrentUser Long currentUserId,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "department", required = false) String department,
            @RequestParam(name = "designation", required = false) String designation,
            @RequestParam(name = "role", required = false) String role) {
        return ResponseEntity
                .ok(employeeService.searchEmployees(currentUserId, query, department, designation, role));
    }

    @PostMapping("/profile/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<?> updateEmployeeProfile(@PathVariable("id") Long id,
            @RequestBody UpdateEmployeeProfileDto updatedProfile) {
        return ResponseEntity.ok(profileService.updateEmployeeProfile(id, updatedProfile));
    }

}
