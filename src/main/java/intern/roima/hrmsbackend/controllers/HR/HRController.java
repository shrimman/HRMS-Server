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
    @PreAuthorize("hasAnyRole('HR')")
    public ResponseEntity<?> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER') or #id == authentication.principal.employeeId")
    public ResponseEntity<EmployeeSummaryDto> getEmployeeById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(profileService.getEmployeeProfileById(id));
    }

    @GetMapping("/searchEmployee/{query}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER')")
    public ResponseEntity<?> searchEmployees(
            @CurrentUser Long currentUserId,
            @PathVariable("query") String query,
            @RequestParam(name = "department", required = false) String department,
            @RequestParam(name = "designation", required = false) String designation,
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity
                .ok(employeeService.searchEmployees(currentUserId, query, department, designation, role, page, size));
    }

    @PostMapping("/profile/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<?> updateEmployeeProfile(@PathVariable("id") Long id,
            @RequestBody EmployeeSummaryDto updatedProfile) {
        return ResponseEntity.ok(profileService.updateEmployeeProfile(id, updatedProfile));
    }

}
