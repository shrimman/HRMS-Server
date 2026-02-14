package intern.roima.hrmsbackend.controllers.Travel;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import intern.roima.hrmsbackend.dtos.Requests.CreateTravelPlanRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateTravelPlanRequest;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.TravelPlanDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Travel_Module.TravelService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/travels")
public class TravelPlanController {

    private final TravelService travelService;

    public TravelPlanController(TravelService travelService) {
        this.travelService = travelService;
    }

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<TravelPlanDto> createTravelPlan(
            @Valid @RequestBody CreateTravelPlanRequest request,
            @CurrentUser Long hrId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(travelService.createTravelPlan(request, hrId));
    }

    @GetMapping("/{travelId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<TravelPlanDto> getTravelPlanById(
            @PathVariable("travelId") Long travelId) {
        return ResponseEntity.ok(travelService.getTravelPlanById(travelId));
    }

    @PutMapping("/{travelId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<TravelPlanDto> updateTravelPlan(
            @PathVariable("travelId") Long travelId,
            @Valid @RequestBody UpdateTravelPlanRequest request,
            @CurrentUser Long hrId) {
        return ResponseEntity.ok(travelService.updateTravelPlan(travelId, request, hrId));
    }

    @DeleteMapping("/{travelId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> deleteTravelPlan(
            @PathVariable("travelId") Long travelId,
            @CurrentUser Long hrId) {
        travelService.deleteTravelPlan(travelId, hrId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<TravelPlanDto>> getAllTravelPlans() {
        return ResponseEntity.ok(travelService.getAllTravelPlans());
    }

    @GetMapping("/my-travels")
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<TravelPlanDto>> getMyTravelPlans(
            @CurrentUser Long employeeId) {
        return ResponseEntity.ok(travelService.getTravelPlansForEmployee(employeeId));
    }

    @GetMapping("/created-by-me")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<TravelPlanDto>> getTravelPlansCreatedByMe(
            @CurrentUser Long hrId) {
        return ResponseEntity.ok(travelService.getTravelPlansCreatedByHR(hrId));
    }

    @GetMapping("/{travelId}/employees")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<EmployeeSummaryDto>> getEmployeesForTravelPlan(
            @PathVariable("travelId") Long travelId) {
        return ResponseEntity.ok(travelService.getEmployeesForTravelPlan(travelId));
    }

    @PostMapping("/{travelId}/employees/{employeeId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> addEmployeeToTravelPlan(
            @PathVariable("travelId") Long travelId,
            @PathVariable("employeeId") Long employeeId,
            @CurrentUser Long hrId) {
        travelService.addEmployeeToTravelPlan(travelId, employeeId, hrId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{travelId}/employees/{employeeId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> removeEmployeeFromTravelPlan(
            @PathVariable("travelId") Long travelId,
            @PathVariable("employeeId") Long employeeId,
            @CurrentUser Long hrId) {
        travelService.removeEmployeeFromTravelPlan(travelId, employeeId, hrId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{travelId}/can-submit-expense")
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<Boolean> canSubmitExpenseForTravel(
            @PathVariable("travelId") Long travelId,
            @CurrentUser Long employeeId) {
        boolean canSubmit = travelService.canSubmitExpenseForTravel(travelId, employeeId);
        return ResponseEntity.ok(canSubmit);
    }
}
