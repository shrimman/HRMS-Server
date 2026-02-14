package intern.roima.hrmsbackend.services.Travel_Module;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import intern.roima.hrmsbackend.dtos.Requests.CreateTravelExpenseRequest;
import intern.roima.hrmsbackend.dtos.Requests.ExpenseFilterRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateTravelExpenseRequest;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.ExpenseReceiptDto;
import intern.roima.hrmsbackend.dtos.Responses.TravelExpenseDto;

public interface TravelExpenseService {

    TravelExpenseDto createExpense(CreateTravelExpenseRequest request, Long employeeId);

    TravelExpenseDto updateExpense(Long expenseId, UpdateTravelExpenseRequest request, Long employeeId);

    TravelExpenseDto submitExpense(Long expenseId, Long employeeId);

    boolean canSubmitExpense(Long expenseId);

    boolean hasMinimumReceipts(Long expenseId);

    boolean validateExpenseAmount(Long expenseId);

    TravelExpenseDto getExpenseById(Long expenseId);

    List<TravelExpenseDto> getExpensesForTravelPlan(Long travelPlanId);

    List<TravelExpenseDto> getExpensesForEmployee(Long employeeId);

    List<TravelExpenseDto> getExpensesByStatus(Long statusId);

    List<TravelExpenseDto> getExpensesByDateRange(LocalDate startDate, LocalDate endDate);

    List<TravelExpenseDto> getExpensesWithFilters(ExpenseFilterRequest filterRequest);

    BigDecimal getTotalExpenseAmountForTravel(Long travelPlanId);

    BigDecimal getTotalExpenseAmountForTravelAndEmployee(Long travelPlanId, Long employeeId);

    TravelExpenseDto approveExpense(Long expenseId, Long hrId, String remarks);

    TravelExpenseDto rejectExpense(Long expenseId, Long hrId, String remarks);

    TravelExpenseDto approveExpenseByManager(Long expenseId, Long managerId, String remarks);

    TravelExpenseDto rejectExpenseByManager(Long expenseId, Long managerId, String remarks);

    List<TravelExpenseDto> getExpensesForManagerApproval(Long managerId);

    void deleteExpense(Long expenseId, Long employeeId);

    ExpenseReceiptDto uploadReceipt(MultipartFile file, Long expenseId, String fileName, Long employeeId);

    List<ExpenseReceiptDto> getReceiptsForExpense(Long expenseId);

    void deleteReceipt(Long receiptId, Long employeeId);

    void addExpenseParticipant(Long expenseId, Long employeeId);

    void removeExpenseParticipant(Long participantId, Long employeeId);

    List<EmployeeSummaryDto> getExpenseParticipants(Long expenseId);

}