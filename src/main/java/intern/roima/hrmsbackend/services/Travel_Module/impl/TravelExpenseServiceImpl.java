package intern.roima.hrmsbackend.services.Travel_Module.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import intern.roima.hrmsbackend.dtos.Requests.CreateTravelExpenseRequest;
import intern.roima.hrmsbackend.dtos.Requests.ExpenseFilterRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateTravelExpenseRequest;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.ExpenseReceiptDto;
import intern.roima.hrmsbackend.dtos.Responses.ExpenseStatusTypeDto;
import intern.roima.hrmsbackend.dtos.Responses.ExpenseTypeDto;
import intern.roima.hrmsbackend.dtos.Responses.TravelExpenseDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.entities.Travel_Module.ExpenseParticipants;
import intern.roima.hrmsbackend.entities.Travel_Module.ExpenseReceipt;
import intern.roima.hrmsbackend.entities.Travel_Module.ExpenseStatus;
import intern.roima.hrmsbackend.entities.Travel_Module.ExpenseType;
import intern.roima.hrmsbackend.entities.Travel_Module.TravelExpenses;
import intern.roima.hrmsbackend.entities.Travel_Module.TravelPlans;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.repositories.Travel_Module.ExpenseParticipantsRepository;
import intern.roima.hrmsbackend.repositories.Travel_Module.ExpenseReceiptRepository;
import intern.roima.hrmsbackend.repositories.Travel_Module.ExpenseStatusRepository;
import intern.roima.hrmsbackend.repositories.Travel_Module.ExpenseTypeRepository;
import intern.roima.hrmsbackend.repositories.Travel_Module.TravelExpenseRepository;
import intern.roima.hrmsbackend.repositories.Travel_Module.TravelPlanRepository;
import intern.roima.hrmsbackend.services.Message_Module.NotificationService;
import intern.roima.hrmsbackend.services.Travel_Module.TravelExpenseService;
import intern.roima.hrmsbackend.services.Utils.FileStorageService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class TravelExpenseServiceImpl implements TravelExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(TravelExpenseServiceImpl.class);
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final int MIN_RECEIPTS_REQUIRED = 1;
    private static final BigDecimal MAX_DAILY_EXPENSE = new BigDecimal("10000");

    private final TravelExpenseRepository travelExpenseRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final EmployeeRepository employeeRepository;
    private final ExpenseTypeRepository expenseTypeRepository;
    private final ExpenseStatusRepository expenseStatusRepository;
    private final ExpenseReceiptRepository expenseReceiptRepository;
    private final ExpenseParticipantsRepository expenseParticipantsRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public TravelExpenseServiceImpl(TravelExpenseRepository travelExpenseRepository,
            TravelPlanRepository travelPlanRepository,
            EmployeeRepository employeeRepository,
            ExpenseTypeRepository expenseTypeRepository,
            ExpenseStatusRepository expenseStatusRepository,
            ExpenseReceiptRepository expenseReceiptRepository,
            ExpenseParticipantsRepository expenseParticipantsRepository,
            FileStorageService fileStorageService,
            NotificationService notificationService) {
        this.travelExpenseRepository = travelExpenseRepository;
        this.travelPlanRepository = travelPlanRepository;
        this.employeeRepository = employeeRepository;
        this.expenseTypeRepository = expenseTypeRepository;
        this.expenseStatusRepository = expenseStatusRepository;
        this.expenseReceiptRepository = expenseReceiptRepository;
        this.expenseParticipantsRepository = expenseParticipantsRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public TravelExpenseDto createExpense(CreateTravelExpenseRequest request, Long employeeId) {
        logger.info("Creating expense for travel plan ID: {} by employee ID: {}",
                request.getTravelPlanId(), employeeId);

        try {
            TravelPlans travelPlan = travelPlanRepository.findById(request.getTravelPlanId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Travel plan not found with ID: " + request.getTravelPlanId()));

            Employees employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            ExpenseType expenseType = expenseTypeRepository.findById(request.getExpenseTypeId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Expense type not found with ID: " + request.getExpenseTypeId()));

            ExpenseStatus draftStatus = expenseStatusRepository.findByStatusName(STATUS_DRAFT)
                    .orElseThrow(() -> new EntityNotFoundException("Draft status not found"));

            TravelExpenses expense = new TravelExpenses();
            expense.setTravelPlan(travelPlan);
            expense.setSubmittedBy(employee);
            expense.setExpenseType(expenseType);
            expense.setAmount(request.getAmount());
            expense.setExpenseDate(request.getExpenseDate());
            expense.setApprovalStatus(draftStatus);
            expense.setSubmittedAt(LocalDateTime.now());
            expense.setUpdatedAt(LocalDateTime.now());
            expense.setUpdatedByEmployee(employee);

            TravelExpenses savedExpense = travelExpenseRepository.save(expense);

            logger.info("Successfully created expense ID: {} for travel plan ID: {}",
                    savedExpense.getExpenseId(), request.getTravelPlanId());

            return toTravelExpenseDto(savedExpense);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error creating expense: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error creating expense: {}", e.getMessage());
            throw new RuntimeException("Database error while creating expense", e);
        }
    }

    @Override
    @Transactional
    public TravelExpenseDto updateExpense(Long expenseId, UpdateTravelExpenseRequest request, Long employeeId) {
        logger.info("Updating expense ID: {} by employee ID: {}", expenseId, employeeId);

        try {
            TravelExpenses expense = travelExpenseRepository.findById(expenseId)
                    .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));

            Employees employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            if (!expense.getApprovalStatus().getStatusName().equals(STATUS_DRAFT)) {
                throw new IllegalArgumentException("Only draft expenses can be updated");
            }

            if (!expense.getSubmittedBy().getEmployeeId().equals(employeeId)) {
                throw new IllegalArgumentException("Only the submitter can update the expense");
            }

            if (request.getExpenseTypeId() != null) {
                ExpenseType expenseType = expenseTypeRepository.findById(request.getExpenseTypeId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Expense type not found with ID: " + request.getExpenseTypeId()));
                expense.setExpenseType(expenseType);
            }

            if (request.getAmount() != null) {
                expense.setAmount(request.getAmount());
            }

            if (request.getExpenseDate() != null) {
                expense.setExpenseDate(request.getExpenseDate());
            }

            expense.setUpdatedAt(LocalDateTime.now());
            expense.setUpdatedByEmployee(employee);

            TravelExpenses updatedExpense = travelExpenseRepository.save(expense);

            logger.info("Successfully updated expense ID: {}", expenseId);

            return toTravelExpenseDto(updatedExpense);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error updating expense: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error updating expense: {}", e.getMessage());
            throw new RuntimeException("Database error while updating expense", e);
        }
    }

    @Override
    @Transactional
    public TravelExpenseDto submitExpense(Long expenseId, Long employeeId) {
        logger.info("Submitting expense ID: {} by employee ID: {}", expenseId, employeeId);

        try {
            TravelExpenses expense = travelExpenseRepository.findById(expenseId)
                    .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));

            if (!expense.getSubmittedBy().getEmployeeId().equals(employeeId)) {
                throw new IllegalArgumentException("Only the submitter can submit the expense");
            }

            if (!expense.getApprovalStatus().getStatusName().equals(STATUS_DRAFT)) {
                throw new IllegalArgumentException("Only draft expenses can be submitted");
            }

            if (!canSubmitExpense(expenseId)) {
                throw new IllegalArgumentException("Expense does not meet submission requirements");
            }

            ExpenseStatus submittedStatus = expenseStatusRepository.findByStatusName(STATUS_SUBMITTED)
                    .orElseThrow(() -> new EntityNotFoundException("Submitted status not found"));

            expense.setApprovalStatus(submittedStatus);
            expense.setUpdatedAt(LocalDateTime.now());

            TravelExpenses submittedExpense = travelExpenseRepository.save(expense);

            notificationService.sendExpenseSubmissionNotification(
                    expense.getTravelPlan().getCreatedByHR().getEmployeeId(),
                    submittedExpense.getExpenseId(),
                    expense.getSubmittedBy().getFirstName() + " " + expense.getSubmittedBy().getLastName(),
                    expense.getTravelPlan().getTitle());
            logger.info("Successfully submitted expense ID: {}", expenseId);

            return toTravelExpenseDto(submittedExpense);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error submitting expense: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error submitting expense: {}", e.getMessage());
            throw new RuntimeException("Database error while submitting expense", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canSubmitExpense(Long expenseId) {
        logger.debug("Checking if expense ID: {} can be submitted", expenseId);

        try {
            return hasMinimumReceipts(expenseId) && validateExpenseAmount(expenseId);

        } catch (EntityNotFoundException e) {
            logger.error("Error checking expense submission eligibility: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMinimumReceipts(Long expenseId) {
        logger.debug("Checking if expense ID: {} has minimum receipts", expenseId);

        List<ExpenseReceipt> receipts = expenseReceiptRepository.findByExpense_ExpenseId(expenseId);
        boolean hasMinimum = receipts.size() >= MIN_RECEIPTS_REQUIRED;

        logger.debug("Expense ID: {} has {} receipts (minimum required: {})",
                expenseId, receipts.size(), MIN_RECEIPTS_REQUIRED);
        logger.info("Expense ID: {} has {} receipts (minimum required: {})",
                expenseId, receipts.size(), MIN_RECEIPTS_REQUIRED, hasMinimum ? "meets" : "does not meet");
        return hasMinimum;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateExpenseAmount(Long expenseId) {
        logger.debug("Validating expense amount for expense ID: {}", expenseId);

        try {
            TravelExpenses expense = travelExpenseRepository.findById(expenseId)
                    .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));

            boolean isValid = expense.getAmount().compareTo(BigDecimal.ZERO) > 0
                    && expense.getAmount().compareTo(MAX_DAILY_EXPENSE) <= 0;

            logger.debug("Expense ID: {} amount {} is valid: {}", expenseId, expense.getAmount(), isValid);

            return isValid;

        } catch (EntityNotFoundException e) {
            logger.error("Error validating expense amount: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TravelExpenseDto getExpenseById(Long expenseId) {
        logger.info("Fetching expense by ID: {}", expenseId);

        try {
            TravelExpenses expense = travelExpenseRepository.findById(expenseId)
                    .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));

            return toTravelExpenseDto(expense);

        } catch (EntityNotFoundException e) {
            logger.error("Error fetching expense: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error fetching expense: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching expense", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelExpenseDto> getExpensesForTravelPlan(Long travelPlanId) {
        logger.info("Fetching expenses for travel plan ID: {}", travelPlanId);

        try {
            List<TravelExpenseDto> expenses = travelExpenseRepository.findByTravelPlan_TravelId(travelPlanId)
                    .stream()
                    .map(this::toTravelExpenseDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} expenses for travel plan ID: {}", expenses.size(), travelPlanId);
            return expenses;

        } catch (DataAccessException e) {
            logger.error("Database error fetching expenses for travel plan: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching expenses for travel plan", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelExpenseDto> getExpensesForEmployee(Long employeeId) {
        logger.info("Fetching expenses for employee ID: {}", employeeId);

        try {
            List<TravelExpenseDto> expenses = travelExpenseRepository.findBySubmittedBy_EmployeeId(employeeId)
                    .stream()
                    .map(this::toTravelExpenseDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} expenses for employee ID: {}", expenses.size(), employeeId);
            return expenses;

        } catch (DataAccessException e) {
            logger.error("Database error fetching expenses for employee: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching expenses for employee", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelExpenseDto> getExpensesByStatus(Long statusId) {
        logger.info("Fetching expenses by status ID: {}", statusId);

        try {
            List<TravelExpenseDto> expenses = travelExpenseRepository.findByApprovalStatus_StatusId(statusId)
                    .stream()
                    .map(this::toTravelExpenseDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} expenses with status ID: {}", expenses.size(), statusId);
            return expenses;

        } catch (DataAccessException e) {
            logger.error("Database error fetching expenses by status: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching expenses by status", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelExpenseDto> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching expenses between {} and {}", startDate, endDate);

        try {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }

            List<TravelExpenseDto> expenses = travelExpenseRepository.findByExpenseDateBetween(startDate, endDate)
                    .stream()
                    .map(this::toTravelExpenseDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} expenses between {} and {}", expenses.size(), startDate, endDate);
            return expenses;

        } catch (IllegalArgumentException e) {
            logger.error("Error fetching expenses by date range: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error fetching expenses by date range: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching expenses by date range", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelExpenseDto> getExpensesWithFilters(ExpenseFilterRequest filterRequest) {
        logger.info("Fetching expenses with filters");

        try {
            List<TravelExpenses> expenses = travelExpenseRepository.findAll();

            if (filterRequest.getEmployeeId() != null) {
                expenses = expenses.stream()
                        .filter(expense -> expense.getSubmittedBy().getEmployeeId()
                                .equals(filterRequest.getEmployeeId()))
                        .collect(Collectors.toList());
            }

            if (filterRequest.getTravelPlanId() != null) {
                expenses = expenses.stream()
                        .filter(expense -> expense.getTravelPlan().getTravelId()
                                .equals(filterRequest.getTravelPlanId()))
                        .collect(Collectors.toList());
            }

            if (filterRequest.getStatusId() != null) {
                expenses = expenses.stream()
                        .filter(expense -> expense.getApprovalStatus().getStatusId()
                                .equals(filterRequest.getStatusId()))
                        .collect(Collectors.toList());
            }

            if (filterRequest.getStartDate() != null && filterRequest.getEndDate() != null) {
                expenses = expenses.stream()
                        .filter(expense -> !expense.getExpenseDate().isBefore(filterRequest.getStartDate())
                                && !expense.getExpenseDate().isAfter(filterRequest.getEndDate()))
                        .collect(Collectors.toList());
            }

            if (filterRequest.getExpenseTypeId() != null) {
                expenses = expenses.stream()
                        .filter(expense -> expense.getExpenseType().getExpenseTypeId()
                                .equals(filterRequest.getExpenseTypeId()))
                        .collect(Collectors.toList());
            }

            List<TravelExpenseDto> result = expenses.stream()
                    .map(this::toTravelExpenseDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} expenses with filters", result.size());
            return result;

        } catch (DataAccessException e) {
            logger.error("Database error fetching expenses with filters: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching expenses with filters", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpenseAmountForTravel(Long travelPlanId) {
        logger.info("Calculating total expense amount for travel plan ID: {}", travelPlanId);

        try {
            BigDecimal total = travelExpenseRepository.getTotalExpenseAmountByTravelPlan(travelPlanId);
            if (total == null) {
                total = BigDecimal.ZERO;
            }

            logger.debug("Total expense amount for travel plan ID: {} is {}", travelPlanId, total);
            return total;

        } catch (DataAccessException e) {
            logger.error("Database error calculating total expense amount: {}", e.getMessage());
            throw new RuntimeException("Database error while calculating total expense amount", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalExpenseAmountForTravelAndEmployee(Long travelPlanId, Long employeeId) {
        logger.info("Calculating total expense amount for travel plan ID: {} and employee ID: {}",
                travelPlanId, employeeId);

        try {
            BigDecimal total = travelExpenseRepository.getTotalExpenseAmountByTravelPlanAndEmployee(travelPlanId,
                    employeeId);
            if (total == null) {
                total = BigDecimal.ZERO;
            }

            logger.debug("Total expense amount for travel plan ID: {} and employee ID: {} is {}",
                    travelPlanId, employeeId, total);
            return total;

        } catch (DataAccessException e) {
            logger.error("Database error calculating total expense amount: {}", e.getMessage());
            throw new RuntimeException("Database error while calculating total expense amount", e);
        }
    }

    @Override
    @Transactional
    public TravelExpenseDto approveExpense(Long expenseId, Long hrId, String remarks) {
        logger.info("Approving expense ID: {} by HR ID: {}", expenseId, hrId);

        try {
            TravelExpenses expense = travelExpenseRepository.findById(expenseId)
                    .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));

            Employees hr = employeeRepository.findById(hrId)
                    .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrId));

            if (!expense.getApprovalStatus().getStatusName().equals(STATUS_SUBMITTED)) {
                throw new IllegalArgumentException("Only submitted expenses can be approved");
            }

            ExpenseStatus approvedStatus = expenseStatusRepository.findByStatusName(STATUS_APPROVED)
                    .orElseThrow(() -> new EntityNotFoundException("Approved status not found"));

            expense.setApprovalStatus(approvedStatus);
            expense.setHrActionBy(hr);
            expense.setHrRemarks(remarks);
            expense.setUpdatedAt(LocalDateTime.now());
            expense.setUpdatedByEmployee(hr);

            TravelExpenses approvedExpense = travelExpenseRepository.save(expense);

            logger.info("Successfully approved expense ID: {}", expenseId);

            return toTravelExpenseDto(approvedExpense);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error approving expense: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error approving expense: {}", e.getMessage());
            throw new RuntimeException("Database error while approving expense", e);
        }
    }

    @Override
    @Transactional
    public TravelExpenseDto rejectExpense(Long expenseId, Long hrId, String remarks) {
        logger.info("Rejecting expense ID: {} by HR ID: {}", expenseId, hrId);

        try {
            if (remarks == null || remarks.trim().isEmpty()) {
                throw new IllegalArgumentException("Remarks are mandatory when rejecting an expense");
            }

            TravelExpenses expense = travelExpenseRepository.findById(expenseId)
                    .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));

            Employees hr = employeeRepository.findById(hrId)
                    .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrId));

            if (!expense.getApprovalStatus().getStatusName().equals(STATUS_SUBMITTED)) {
                throw new IllegalArgumentException("Only submitted expenses can be rejected");
            }

            ExpenseStatus rejectedStatus = expenseStatusRepository.findByStatusName(STATUS_REJECTED)
                    .orElseThrow(() -> new EntityNotFoundException("Rejected status not found"));

            expense.setApprovalStatus(rejectedStatus);
            expense.setHrActionBy(hr);
            expense.setHrRemarks(remarks);
            expense.setUpdatedAt(LocalDateTime.now());
            expense.setUpdatedByEmployee(hr);

            TravelExpenses rejectedExpense = travelExpenseRepository.save(expense);

            logger.info("Successfully rejected expense ID: {}", expenseId);

            return toTravelExpenseDto(rejectedExpense);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error rejecting expense: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error rejecting expense: {}", e.getMessage());
            throw new RuntimeException("Database error while rejecting expense", e);
        }
    }

    @Override
    @Transactional
    public TravelExpenseDto approveExpenseByManager(Long expenseId, Long managerId, String remarks) {
        logger.info("Approving expense ID: {} by manager ID: {}", expenseId, managerId);

        try {
            TravelExpenses expense = travelExpenseRepository.findById(expenseId)
                    .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));

            Employees manager = employeeRepository.findById(managerId)
                    .orElseThrow(() -> new EntityNotFoundException("Manager not found with ID: " + managerId));

            if (!expense.getSubmittedBy().getManager().getEmployeeId().equals(managerId)) {
                throw new IllegalArgumentException("Only the direct manager can approve this expense");
            }

            if (!expense.getApprovalStatus().getStatusName().equals(STATUS_SUBMITTED)) {
                throw new IllegalArgumentException("Only submitted expenses can be approved");
            }

            ExpenseStatus approvedStatus = expenseStatusRepository.findByStatusName(STATUS_APPROVED)
                    .orElseThrow(() -> new EntityNotFoundException("Approved status not found"));

            expense.setApprovalStatus(approvedStatus);
            expense.setHrActionBy(manager);
            expense.setHrRemarks(remarks);
            expense.setUpdatedAt(LocalDateTime.now());
            expense.setUpdatedByEmployee(manager);

            TravelExpenses approvedExpense = travelExpenseRepository.save(expense);
            notificationService.sendExpenseStatusNotification(
                    expense.getSubmittedBy().getEmployeeId(),
                    approvedExpense.getExpenseId(),
                    STATUS_APPROVED,
                    remarks);
            logger.info("Successfully approved expense ID: {} by manager", expenseId);

            return toTravelExpenseDto(approvedExpense);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error approving expense by manager: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error approving expense by manager: {}", e.getMessage());
            throw new RuntimeException("Database error while approving expense by manager", e);
        }
    }

    @Override
    @Transactional
    public TravelExpenseDto rejectExpenseByManager(Long expenseId, Long managerId, String remarks) {
        logger.info("Rejecting expense ID: {} by manager ID: {}", expenseId, managerId);

        try {
            if (remarks == null || remarks.trim().isEmpty()) {
                throw new IllegalArgumentException("Remarks are mandatory when rejecting an expense");
            }

            TravelExpenses expense = travelExpenseRepository.findById(expenseId)
                    .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));

            Employees manager = employeeRepository.findById(managerId)
                    .orElseThrow(() -> new EntityNotFoundException("Manager not found with ID: " + managerId));

            if (!expense.getSubmittedBy().getManager().getEmployeeId().equals(managerId)) {
                throw new IllegalArgumentException("Only the direct manager can reject this expense");
            }

            if (!expense.getApprovalStatus().getStatusName().equals(STATUS_SUBMITTED)) {
                throw new IllegalArgumentException("Only submitted expenses can be rejected");
            }

            ExpenseStatus rejectedStatus = expenseStatusRepository.findByStatusName(STATUS_REJECTED)
                    .orElseThrow(() -> new EntityNotFoundException("Rejected status not found"));

            expense.setApprovalStatus(rejectedStatus);
            expense.setHrActionBy(manager);
            expense.setHrRemarks(remarks);
            expense.setUpdatedAt(LocalDateTime.now());
            expense.setUpdatedByEmployee(manager);

            TravelExpenses rejectedExpense = travelExpenseRepository.save(expense);
            notificationService.sendExpenseStatusNotification(
                    expense.getSubmittedBy().getEmployeeId(),
                    rejectedExpense.getExpenseId(),
                    STATUS_REJECTED,
                    remarks);
            logger.info("Successfully rejected expense ID: {} by manager", expenseId);

            return toTravelExpenseDto(rejectedExpense);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error rejecting expense by manager: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error rejecting expense by manager: {}", e.getMessage());
            throw new RuntimeException("Database error while rejecting expense by manager", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelExpenseDto> getExpensesForManagerApproval(Long managerId) {
        logger.info("Fetching expenses for manager ID: {} approval", managerId);

        try {
            List<Employees> teamMembers = employeeRepository.findByManagerEmployeeId(managerId);

            ExpenseStatus submittedStatus = expenseStatusRepository.findByStatusName(STATUS_SUBMITTED)
                    .orElseThrow(() -> new EntityNotFoundException("Submitted status not found"));

            List<TravelExpenseDto> expenses = teamMembers.stream()
                    .flatMap(employee -> travelExpenseRepository.findBySubmittedBy_EmployeeId(employee.getEmployeeId())
                            .stream())
                    .filter(expense -> expense.getApprovalStatus().getStatusId().equals(submittedStatus.getStatusId()))
                    .map(this::toTravelExpenseDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} expenses for manager ID: {} approval", expenses.size(), managerId);
            return expenses;

        } catch (EntityNotFoundException e) {
            logger.error("Error fetching expenses for manager approval: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error fetching expenses for manager approval: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching expenses for manager approval", e);
        }
    }

    @Override
    @Transactional
    public void deleteExpense(Long expenseId, Long employeeId) {
        logger.info("Deleting expense ID: {} by employee ID: {}", expenseId, employeeId);

        try {
            TravelExpenses expense = travelExpenseRepository.findById(expenseId)
                    .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));

            employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            if (!expense.getSubmittedBy().getEmployeeId().equals(employeeId)) {
                throw new IllegalArgumentException("Only the submitter can delete the expense");
            }

            if (!expense.getApprovalStatus().getStatusName().equals(STATUS_DRAFT)) {
                throw new IllegalArgumentException("Only draft expenses can be deleted");
            }

            List<ExpenseParticipants> participants = expenseParticipantsRepository.findByExpense_ExpenseId(expenseId);
            expenseParticipantsRepository.deleteAll(participants);

            List<ExpenseReceipt> receipts = expenseReceiptRepository.findByExpense_ExpenseId(expenseId);
            for (ExpenseReceipt receipt : receipts) {
                if (fileStorageService.fileExists(receipt.getReceiptPath())) {
                    fileStorageService.deleteFile(receipt.getReceiptPath());
                }
            }
            expenseReceiptRepository.deleteAll(receipts);

            travelExpenseRepository.delete(expense);

            logger.info("Successfully deleted expense ID: {}", expenseId);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error deleting expense: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error deleting expense: {}", e.getMessage());
            throw new RuntimeException("Database error while deleting expense", e);
        }
    }

    @Override
    @Transactional
    public ExpenseReceiptDto uploadReceipt(MultipartFile file, Long expenseId, String fileName, Long employeeId) {
        logger.info("Uploading receipt for expense ID: {} by employee ID: {}", expenseId, employeeId);

        try {
            TravelExpenses expense = travelExpenseRepository.findById(expenseId)
                    .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));

            Employees employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            if (!expense.getSubmittedBy().getEmployeeId().equals(employeeId)) {
                throw new IllegalArgumentException("Only the expense submitter can upload receipts");
            }

            if (file.isEmpty()) {
                throw new IllegalArgumentException("Cannot upload empty file");
            }

            String filePath = fileStorageService.storeExpenseProof(file, expenseId);

            ExpenseReceipt receipt = new ExpenseReceipt();
            receipt.setExpense(expense);
            receipt.setFileName(fileName);
            receipt.setReceiptPath(filePath);
            receipt.setUploadedAt(LocalDateTime.now());
            receipt.setUpdatedAt(LocalDateTime.now());
            receipt.setUpdatedByEmployee(employee);

            ExpenseReceipt savedReceipt = expenseReceiptRepository.save(receipt);

            logger.info("Successfully uploaded receipt ID: {} for expense ID: {}",
                    savedReceipt.getExpenseReceiptId(), expenseId);

            return toExpenseReceiptDto(savedReceipt);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error uploading receipt: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error uploading receipt: {}", e.getMessage());
            throw new RuntimeException("Database error while uploading receipt", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseReceiptDto> getReceiptsForExpense(Long expenseId) {
        logger.info("Fetching receipts for expense ID: {}", expenseId);

        try {
            List<ExpenseReceiptDto> receipts = expenseReceiptRepository.findByExpense_ExpenseId(expenseId)
                    .stream()
                    .map(this::toExpenseReceiptDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} receipts for expense ID: {}", receipts.size(), expenseId);
            return receipts;

        } catch (DataAccessException e) {
            logger.error("Database error fetching receipts: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching receipts", e);
        }
    }

    @Override
    @Transactional
    public void deleteReceipt(Long receiptId, Long employeeId) {
        logger.info("Deleting receipt ID: {} by employee ID: {}", receiptId, employeeId);

        try {
            ExpenseReceipt receipt = expenseReceiptRepository.findById(receiptId)
                    .orElseThrow(() -> new EntityNotFoundException("Receipt not found with ID: " + receiptId));

            if (!receipt.getExpense().getSubmittedBy().getEmployeeId().equals(employeeId)) {
                throw new IllegalArgumentException("Only the expense submitter can delete receipts");
            }

            if (!receipt.getExpense().getApprovalStatus().getStatusName().equals(STATUS_DRAFT)) {
                throw new IllegalArgumentException("Receipts can only be deleted from draft expenses");
            }

            if (fileStorageService.fileExists(receipt.getReceiptPath())) {
                fileStorageService.deleteFile(receipt.getReceiptPath());
            }

            expenseReceiptRepository.delete(receipt);

            logger.info("Successfully deleted receipt ID: {}", receiptId);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error deleting receipt: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error deleting receipt: {}", e.getMessage());
            throw new RuntimeException("Database error while deleting receipt", e);
        }
    }

    @Override
    @Transactional
    public void addExpenseParticipant(Long expenseId, Long employeeIdToAdd, Long currentUserId) {
        logger.info("Adding participant employee ID: {} to expense ID: {} by employee ID: {}", employeeIdToAdd,
                expenseId, currentUserId);

        try {
            TravelExpenses expense = travelExpenseRepository.findById(expenseId)
                    .orElseThrow(() -> new EntityNotFoundException("Expense not found with ID: " + expenseId));

            if (!expense.getSubmittedBy().getEmployeeId().equals(currentUserId)) {
                logger.warn("User {} attempted to add participant to expense {} submitted by {}",
                        currentUserId, expenseId, expense.getSubmittedBy().getEmployeeId());
                throw new IllegalArgumentException("Only the expense submitter can add participants");
            }

            Employees employee = employeeRepository.findById(employeeIdToAdd)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeIdToAdd));

            List<ExpenseParticipants> existingParticipants = expenseParticipantsRepository
                    .findByExpense_ExpenseId(expenseId);
            boolean alreadyParticipant = existingParticipants.stream()
                    .anyMatch(p -> p.getEmployee().getEmployeeId().equals(employeeIdToAdd));

            if (alreadyParticipant) {
                throw new IllegalArgumentException("Employee is already a participant in this expense");
            }

            ExpenseParticipants participant = new ExpenseParticipants();
            participant.setExpense(expense);
            participant.setEmployee(employee);
            participant.setCreatedAt(LocalDateTime.now());
            participant.setUpdatedAt(LocalDateTime.now());
            participant.setUpdatedByEmployee(expense.getSubmittedBy());

            expenseParticipantsRepository.save(participant);

            logger.info("Successfully added participant employee ID: {} to expense ID: {}", employeeIdToAdd, expenseId);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error adding expense participant: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error adding expense participant: {}", e.getMessage());
            throw new RuntimeException("Database error while adding expense participant", e);
        }
    }

    @Override
    @Transactional
    public void removeExpenseParticipant(Long expenseId, Long participantId, Long employeeId) {
        logger.info("Removing participant with employee ID: {} from expense ID: {} by employee ID: {}", participantId,
                expenseId, employeeId);

        try {
            ExpenseParticipants participant = expenseParticipantsRepository
                    .findByExpense_ExpenseIdAndEmployee_EmployeeId(expenseId, participantId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Participant not found with employee ID: " + participantId + " in expense ID: "
                                    + expenseId));

            logger.info("Found participant ID: {} for employee ID: {} in expense ID: {}",
                    participant.getParticipantId(), participant.getEmployee().getEmployeeId(),
                    participant.getExpense().getExpenseId());
            logger.info("Current user employee ID (from @CurrentUser): {}", employeeId);
            logger.info("Expense submitter employee ID: {}", participant.getExpense().getSubmittedBy().getEmployeeId());

            if (!participant.getExpense().getSubmittedBy().getEmployeeId().equals(employeeId)) {
                logger.warn("User {} attempted to remove participant from expense {} submitted by {}",
                        employeeId, participant.getExpense().getExpenseId(),
                        participant.getExpense().getSubmittedBy().getEmployeeId());
                throw new IllegalArgumentException("Only the expense submitter can remove participants");
            }

            if (!participant.getExpense().getApprovalStatus().getStatusName().equals(STATUS_DRAFT)) {
                throw new IllegalArgumentException("Participants can only be removed from draft expenses");
            }

            expenseParticipantsRepository.delete(participant);

            logger.info("Successfully removed participant ID: {} (employee ID: {}) from expense",
                    participant.getParticipantId(), participantId);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error removing expense participant: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error removing expense participant: {}", e.getMessage());
            throw new RuntimeException("Database error while removing expense participant", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSummaryDto> getExpenseParticipants(Long expenseId) {
        logger.info("Fetching participants for expense ID: {}", expenseId);

        try {
            List<EmployeeSummaryDto> participants = expenseParticipantsRepository.findByExpense_ExpenseId(expenseId)
                    .stream()
                    .map(participant -> toEmployeeSummaryDto(participant.getEmployee()))
                    .collect(Collectors.toList());

            logger.debug("Found {} participants for expense ID: {}", participants.size(), expenseId);
            return participants;

        } catch (DataAccessException e) {
            logger.error("Database error fetching expense participants: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching expense participants", e);
        }
    }

    private TravelExpenseDto toTravelExpenseDto(TravelExpenses expense) {
        TravelExpenseDto dto = new TravelExpenseDto();
        dto.setExpenseId(expense.getExpenseId());
        dto.setTravelPlanId(expense.getTravelPlan().getTravelId());
        dto.setTravelPlanTitle(expense.getTravelPlan().getTitle());
        dto.setExpenseTypeId(expense.getExpenseType().getExpenseTypeId());
        dto.setExpenseTypeName(expense.getExpenseType().getTypeName());
        dto.setAmount(expense.getAmount());
        dto.setExpenseDate(expense.getExpenseDate());
        dto.setSubmittedAt(expense.getSubmittedAt());
        dto.setUpdatedAt(expense.getUpdatedAt());
        dto.setHrRemarks(expense.getHrRemarks());
        dto.setApprovalStatusId(expense.getApprovalStatus().getStatusId());
        dto.setApprovalStatusName(expense.getApprovalStatus().getStatusName());

        if (expense.getHrActionBy() != null) {
            dto.setHrActionBy(toEmployeeSummaryDto(expense.getHrActionBy()));
        }

        dto.setSubmittedBy(toEmployeeSummaryDto(expense.getSubmittedBy()));

        List<ExpenseReceipt> receipts = expenseReceiptRepository.findByExpense_ExpenseId(expense.getExpenseId());
        dto.setReceipts(receipts.stream().map(this::toExpenseReceiptDto).collect(Collectors.toList()));

        List<ExpenseParticipants> participants = expenseParticipantsRepository
                .findByExpense_ExpenseId(expense.getExpenseId());
        dto.setParticipants(participants.stream()
                .map(participant -> toEmployeeSummaryDto(participant.getEmployee()))
                .collect(Collectors.toList()));

        return dto;
    }

    private ExpenseReceiptDto toExpenseReceiptDto(ExpenseReceipt receipt) {
        ExpenseReceiptDto dto = new ExpenseReceiptDto();
        dto.setExpenseReceiptId(receipt.getExpenseReceiptId());
        dto.setFileName(receipt.getFileName());
        dto.setReceiptPath(receipt.getReceiptPath());
        dto.setUploadedAt(receipt.getUploadedAt());
        dto.setUpdatedAt(receipt.getUpdatedAt());

        if (receipt.getUpdatedByEmployee() != null) {
            dto.setUpdatedByEmployee(toEmployeeSummaryDto(receipt.getUpdatedByEmployee()));
        }

        return dto;
    }

    private EmployeeSummaryDto toEmployeeSummaryDto(Employees employee) {
        EmployeeSummaryDto dto = new EmployeeSummaryDto();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setRole(employee.getRole());
        dto.setDepartment(employee.getDepartment());
        dto.setDesignation(employee.getDesignation());
        dto.setActive(employee.isActive());
        dto.setPhotoPath(employee.getPhotoPath());
        dto.setDateOfBirth(employee.getDateOfBirth());
        dto.setDateOfJoining(employee.getDateOfJoining());

        if (employee.getManager() != null) {
            dto.setManagerId(employee.getManager().getEmployeeId());
            dto.setManagerName(employee.getManager().getFirstName() + " " + employee.getManager().getLastName());
        }

        return dto;
    }

    @Override
    public List<ExpenseStatusTypeDto> getExpenseStatusTypes() {
        return expenseStatusRepository.findAll().stream()
                .map(et -> new ExpenseStatusTypeDto(et.getStatusId(), et.getStatusName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseTypeDto> getAllExpenseTypes() {
        return expenseTypeRepository.findAll().stream()
                .map(et -> new ExpenseTypeDto(
                        et.getExpenseTypeId(),
                        et.getTypeName()))
                .collect(Collectors.toList());
    }
}
