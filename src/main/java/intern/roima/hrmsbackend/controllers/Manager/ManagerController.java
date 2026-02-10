package intern.roima.hrmsbackend.controllers.Manager;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Employee_Module.ManagerTeamService;

@RestController
@RequestMapping("/api/managers")
public class ManagerController {

    private final ManagerTeamService managerTeamService;

    public ManagerController(ManagerTeamService managerTeamService) {
        this.managerTeamService = managerTeamService;
    }

    @GetMapping("/myTeam")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<EmployeeSummaryDto>> getMyTeam(@CurrentUser Long managerId) {
        List<EmployeeSummaryDto> team = managerTeamService.getMyTeam(managerId);
        return ResponseEntity.ok(team);
    }

}
