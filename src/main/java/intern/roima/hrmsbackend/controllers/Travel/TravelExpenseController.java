package intern.roima.hrmsbackend.controllers.Travel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import intern.roima.hrmsbackend.dtos.Requests.ApproveExpenseRequest;
import intern.roima.hrmsbackend.dtos.Requests.CreateTravelExpenseRequest;
import intern.roima.hrmsbackend.dtos.Requests.ExpenseFilterRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateTravelExpenseRequest;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.ExpenseReceiptDto;
import intern.roima.hrmsbackend.dtos.Responses.TravelExpenseDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Travel_Module.TravelExpenseService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/travel-expenses")
public class TravelExpenseController {

    private final TravelExpenseService travelExpenseService;

    public TravelExpenseController(TravelExpenseService travelExpenseService) {
        this.travelExpenseService = travelExpenseService;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TravelExpenseDto> createExpense(
            @Valid @RequestBody CreateTravelExpenseRequest request,
            @CurrentUser Long employeeId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(travelExpenseService.createExpense(request, employeeId));
    }

    @PutMapping("/{expenseId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TravelExpenseDto> updateExpense(
            @PathVariable("expenseId") Long expenseId,
            @Valid @RequestBody UpdateTravelExpenseRequest request,
            @CurrentUser Long employeeId) {
        return ResponseEntity.ok(travelExpenseService.updateExpense(expenseId, request, employeeId));
    }

    @PostMapping("/{expenseId}/submit")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<TravelExpenseDto> submitExpense(
            @PathVariable("expenseId") Long expenseId,
            @CurrentUser Long employeeId) {
        return ResponseEntity.ok(travelExpenseService.submitExpense(expenseId, employeeId));
    }

    @GetMapping("/{expenseId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<TravelExpenseDto> getExpenseById(
            @PathVariable("expenseId") Long expenseId) {
        return ResponseEntity.ok(travelExpenseService.getExpenseById(expenseId));
    }

    @GetMapping("/travel/{travelId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<TravelExpenseDto>> getExpensesForTravelPlan(
            @PathVariable("travelId") Long travelId) {
        return ResponseEntity.ok(travelExpenseService.getExpensesForTravelPlan(travelId));
    }

    @GetMapping("/my-expenses")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<TravelExpenseDto>> getMyExpenses(
            @CurrentUser Long employeeId) {
        return ResponseEntity.ok(travelExpenseService.getExpensesForEmployee(employeeId));
    }

    @GetMapping("/status/{statusId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER')")
    public ResponseEntity<List<TravelExpenseDto>> getExpensesByStatus(
            @PathVariable("statusId") Long statusId) {
        return ResponseEntity.ok(travelExpenseService.getExpensesByStatus(statusId));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER')")
    public ResponseEntity<List<TravelExpenseDto>> getExpensesByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(travelExpenseService.getExpensesByDateRange(startDate, endDate));
    }

    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER')")
    public ResponseEntity<List<TravelExpenseDto>> getExpensesWithFilters(
            @Valid @RequestBody ExpenseFilterRequest filterRequest) {
        return ResponseEntity.ok(travelExpenseService.getExpensesWithFilters(filterRequest));
    }

    @GetMapping("/travel/{travelId}/total")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<BigDecimal> getTotalExpenseAmountForTravel(
            @PathVariable("travelId") Long travelId) {
        return ResponseEntity.ok(travelExpenseService.getTotalExpenseAmountForTravel(travelId));
    }

    @GetMapping("/travel/{travelId}/employee/{employeeId}/total")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<BigDecimal> getTotalExpenseAmountForTravelAndEmployee(
            @PathVariable("travelId") Long travelId,
            @PathVariable("employeeId") Long employeeId) {
        return ResponseEntity.ok(travelExpenseService.getTotalExpenseAmountForTravelAndEmployee(travelId, employeeId));
    }

    @PostMapping("/{expenseId}/approve")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<TravelExpenseDto> approveExpense(
            @PathVariable("expenseId") Long expenseId,
            @Valid @RequestBody ApproveExpenseRequest request,
            @CurrentUser Long hrId) {
        return ResponseEntity.ok(travelExpenseService.approveExpense(expenseId, hrId, request.getRemarks()));
    }

    @PostMapping("/{expenseId}/reject")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<TravelExpenseDto> rejectExpense(
            @PathVariable("expenseId") Long expenseId,
            @Valid @RequestBody ApproveExpenseRequest request,
            @CurrentUser Long hrId) {
        return ResponseEntity.ok(travelExpenseService.rejectExpense(expenseId, hrId, request.getRemarks()));
    }

    @PostMapping("/{expenseId}/approve-by-manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TravelExpenseDto> approveExpenseByManager(
            @PathVariable("expenseId") Long expenseId,
            @Valid @RequestBody ApproveExpenseRequest request,
            @CurrentUser Long managerId) {
        return ResponseEntity.ok(travelExpenseService.approveExpenseByManager(expenseId, managerId, request.getRemarks()));
    }

    @PostMapping("/{expenseId}/reject-by-manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TravelExpenseDto> rejectExpenseByManager(
            @PathVariable("expenseId") Long expenseId,
            @Valid @RequestBody ApproveExpenseRequest request,
            @CurrentUser Long managerId) {
        return ResponseEntity.ok(travelExpenseService.rejectExpenseByManager(expenseId, managerId, request.getRemarks()));
    }

    @GetMapping("/manager/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TravelExpenseDto>> getExpensesForManagerApproval(
            @CurrentUser Long managerId) {
        return ResponseEntity.ok(travelExpenseService.getExpensesForManagerApproval(managerId));
    }

    @DeleteMapping("/{expenseId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable("expenseId") Long expenseId,
            @CurrentUser Long employeeId) {
        travelExpenseService.deleteExpense(expenseId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{expenseId}/receipts/upload")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ExpenseReceiptDto> uploadReceipt(
            @PathVariable("expenseId") Long expenseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileName", required = false) String fileName,
            @CurrentUser Long employeeId) {
        String finalFileName = fileName != null ? fileName : file.getOriginalFilename();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(travelExpenseService.uploadReceipt(file, expenseId, finalFileName, employeeId));
    }

    @GetMapping("/{expenseId}/receipts")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<ExpenseReceiptDto>> getReceiptsForExpense(
            @PathVariable("expenseId") Long expenseId) {
        return ResponseEntity.ok(travelExpenseService.getReceiptsForExpense(expenseId));
    }

    @DeleteMapping("/receipts/{receiptId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> deleteReceipt(
            @PathVariable("receiptId") Long receiptId,
            @CurrentUser Long employeeId) {
        travelExpenseService.deleteReceipt(receiptId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{expenseId}/participants/{participantId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> addExpenseParticipant(
            @PathVariable("expenseId") Long expenseId,
            @PathVariable("participantId") Long participantId,
            @CurrentUser Long employeeId) {
        travelExpenseService.addExpenseParticipant(expenseId, participantId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{expenseId}/participants/{participantId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> removeExpenseParticipant(
            @PathVariable("participantId") Long participantId,
            @CurrentUser Long employeeId) {
        travelExpenseService.removeExpenseParticipant(participantId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{expenseId}/participants")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<EmployeeSummaryDto>> getExpenseParticipants(
            @PathVariable("expenseId") Long expenseId) {
        return ResponseEntity.ok(travelExpenseService.getExpenseParticipants(expenseId));
    }
}
