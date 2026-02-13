package intern.roima.hrmsbackend.services.Travel_Module.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intern.roima.hrmsbackend.dtos.Requests.CreateTravelPlanRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateTravelPlanRequest;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.TravelPlanDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.entities.Travel_Module.TravelPlans;
import intern.roima.hrmsbackend.entities.Travel_Module.Travels;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.repositories.Travel_Module.TravelPlanRepository;
import intern.roima.hrmsbackend.repositories.Travel_Module.TravelRepository;
import intern.roima.hrmsbackend.services.Message_Module.NotificationService;
import intern.roima.hrmsbackend.services.Travel_Module.TravelService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class TravelServiceImpl implements TravelService {

    private static final Logger logger = LoggerFactory.getLogger(TravelServiceImpl.class);
    private static final int EXPENSE_SUBMISSION_GRACE_DAYS = 10;

    private final TravelPlanRepository travelPlanRepository;
    private final TravelRepository travelRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public TravelServiceImpl(TravelPlanRepository travelPlanRepository,
            TravelRepository travelRepository,
            EmployeeRepository employeeRepository, NotificationService notificationService) {
        this.travelPlanRepository = travelPlanRepository;
        this.travelRepository = travelRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public TravelPlanDto createTravelPlan(CreateTravelPlanRequest request, Long hrId) {
        logger.info("Creating travel plan with title: {} by HR ID: {}", request.getTitle(), hrId);

        try {
            Employees hr = employeeRepository.findById(hrId)
                    .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrId));

            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }

            TravelPlans travelPlan = new TravelPlans();
            travelPlan.setTitle(request.getTitle());
            travelPlan.setDescription(request.getDescription());
            travelPlan.setStartDate(request.getStartDate());
            travelPlan.setEndDate(request.getEndDate());
            travelPlan.setCreatedByHR(hr);
            travelPlan.setCreatedAt(LocalDateTime.now());
            travelPlan.setUpdatedAt(LocalDateTime.now());
            travelPlan.setUpdatedByEmployee(hr);

            TravelPlans savedTravelPlan = travelPlanRepository.save(travelPlan);

            for (Long employeeId : request.getEmployeeIds()) {
                Employees employee = employeeRepository.findById(employeeId)
                        .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

                Travels travel = new Travels();
                travel.setTravelPlan(savedTravelPlan);
                travel.setEmployee(employee);
                travel.setCreatedAt(LocalDateTime.now());
                travel.setUpdatedAt(LocalDateTime.now());
                travel.setUpdatedByEmployee(hr);

                travelRepository.save(travel);

            }

            notificationService.sendTravelAssignmentNotifications(request.getEmployeeIds(),
                    savedTravelPlan.getTravelId(), savedTravelPlan.getTitle());

            logger.info("Successfully created travel plan ID: {} with {} employees",
                    savedTravelPlan.getTravelId(), request.getEmployeeIds().size());

            return toTravelPlanDto(savedTravelPlan);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error creating travel plan: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error creating travel plan: {}", e.getMessage());
            throw new RuntimeException("Database error while creating travel plan", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TravelPlanDto getTravelPlanById(Long travelPlanId) {
        logger.info("Fetching travel plan by ID: {}", travelPlanId);

        try {
            TravelPlans travelPlan = travelPlanRepository.findById(travelPlanId)
                    .orElseThrow(() -> new EntityNotFoundException("Travel plan not found with ID: " + travelPlanId));

            return toTravelPlanDto(travelPlan);

        } catch (EntityNotFoundException e) {
            logger.error("Error fetching travel plan: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error fetching travel plan: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching travel plan", e);
        }
    }

    @Override
    @Transactional
    public TravelPlanDto updateTravelPlan(Long travelPlanId, UpdateTravelPlanRequest request, Long hrId) {
        logger.info("Updating travel plan ID: {} by HR ID: {}", travelPlanId, hrId);

        try {
            TravelPlans travelPlan = travelPlanRepository.findById(travelPlanId)
                    .orElseThrow(() -> new EntityNotFoundException("Travel plan not found with ID: " + travelPlanId));

            Employees hr = employeeRepository.findById(hrId)
                    .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrId));

            if (request.getTitle() != null) {
                travelPlan.setTitle(request.getTitle());
            }
            if (request.getDescription() != null) {
                travelPlan.setDescription(request.getDescription());
            }
            if (request.getStartDate() != null) {
                travelPlan.setStartDate(request.getStartDate());
            }
            if (request.getEndDate() != null) {
                travelPlan.setEndDate(request.getEndDate());
            }

            if (travelPlan.getStartDate().isAfter(travelPlan.getEndDate())) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }

            travelPlan.setUpdatedAt(LocalDateTime.now());
            travelPlan.setUpdatedByEmployee(hr);

            if (request.getEmployeeIds() != null && !request.getEmployeeIds().isEmpty()) {
                List<Travels> existingTravels = travelRepository.findByTravelPlan_TravelId(travelPlanId);
                travelRepository.deleteAll(existingTravels);

                for (Long employeeId : request.getEmployeeIds()) {
                    Employees employee = employeeRepository.findById(employeeId)
                            .orElseThrow(
                                    () -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

                    Travels travel = new Travels();
                    travel.setTravelPlan(travelPlan);
                    travel.setEmployee(employee);
                    travel.setCreatedAt(LocalDateTime.now());
                    travel.setUpdatedAt(LocalDateTime.now());
                    travel.setUpdatedByEmployee(hr);

                    travelRepository.save(travel);
                }
            }

            TravelPlans updatedTravelPlan = travelPlanRepository.save(travelPlan);

            notificationService.sendTravelAssignmentNotifications(request.getEmployeeIds(),
                    updatedTravelPlan.getTravelId(), updatedTravelPlan.getTitle());

            logger.info("Successfully updated travel plan ID: {}", travelPlanId);

            return toTravelPlanDto(updatedTravelPlan);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error updating travel plan: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error updating travel plan: {}", e.getMessage());
            throw new RuntimeException("Database error while updating travel plan", e);
        }
    }

    @Override
    @Transactional
    public void deleteTravelPlan(Long travelPlanId, Long hrId) {
        logger.info("Deleting travel plan ID: {} by HR ID: {}", travelPlanId, hrId);

        try {
            TravelPlans travelPlan = travelPlanRepository.findById(travelPlanId)
                    .orElseThrow(() -> new EntityNotFoundException("Travel plan not found with ID: " + travelPlanId));

            employeeRepository.findById(hrId)
                    .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrId));

            List<Travels> travels = travelRepository.findByTravelPlan_TravelId(travelPlanId);
            travelRepository.deleteAll(travels);

            travelPlanRepository.delete(travelPlan);

            logger.info("Successfully deleted travel plan ID: {}", travelPlanId);

        } catch (EntityNotFoundException e) {
            logger.error("Error deleting travel plan: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error deleting travel plan: {}", e.getMessage());
            throw new RuntimeException("Database error while deleting travel plan", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelPlanDto> getAllTravelPlans() {
        logger.info("Fetching all travel plans");

        try {
            List<TravelPlanDto> travelPlans = travelPlanRepository.findAll()
                    .stream()
                    .map(this::toTravelPlanDto)
                    .toList();

            logger.debug("Successfully fetched {} travel plans", travelPlans.size());
            return travelPlans;

        } catch (DataAccessException e) {
            logger.error("Database error fetching all travel plans: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching travel plans", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelPlanDto> getTravelPlansForEmployee(Long employeeId) {
        logger.info("Fetching travel plans for employee ID: {}", employeeId);

        try {
            employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            List<Travels> travels = travelRepository.findByEmployee_EmployeeId(employeeId);

            List<TravelPlanDto> travelPlans = travels.stream()
                    .map(travel -> toTravelPlanDto(travel.getTravelPlan()))
                    .toList();

            logger.debug("Found {} travel plans for employee ID: {}", travelPlans.size(), employeeId);
            return travelPlans;

        } catch (EntityNotFoundException e) {
            logger.error("Error fetching travel plans for employee: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error fetching travel plans for employee: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching travel plans for employee", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelPlanDto> getTravelPlansCreatedByHR(Long hrId) {
        logger.info("Fetching travel plans created by HR ID: {}", hrId);

        try {
            employeeRepository.findById(hrId)
                    .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrId));

            List<TravelPlanDto> travelPlans = travelPlanRepository.findByCreatedByHR_EmployeeId(hrId)
                    .stream()
                    .map(this::toTravelPlanDto)
                    .toList();

            logger.debug("Found {} travel plans created by HR ID: {}", travelPlans.size(), hrId);
            return travelPlans;

        } catch (EntityNotFoundException e) {
            logger.error("Error fetching travel plans created by HR: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error fetching travel plans created by HR: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching travel plans created by HR", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSummaryDto> getEmployeesForTravelPlan(Long travelPlanId) {
        logger.info("Fetching employees for travel plan ID: {}", travelPlanId);

        try {
            List<Travels> travels = travelRepository.findByTravelPlan_TravelId(travelPlanId);

            List<EmployeeSummaryDto> employees = travels.stream()
                    .map(travel -> toEmployeeSummaryDto(travel.getEmployee()))
                    .toList();

            logger.debug("Found {} employees for travel plan ID: {}", employees.size(), travelPlanId);
            return employees;

        } catch (DataAccessException e) {
            logger.error("Database error fetching employees for travel plan: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching employees for travel plan", e);
        }
    }

    @Override
    @Transactional
    public void addEmployeeToTravelPlan(Long travelPlanId, Long employeeId, Long hrId) {
        logger.info("Adding employee ID: {} to travel plan ID: {} by HR ID: {}", employeeId, travelPlanId, hrId);

        try {
            TravelPlans travelPlan = travelPlanRepository.findById(travelPlanId)
                    .orElseThrow(() -> new EntityNotFoundException("Travel plan not found with ID: " + travelPlanId));

            Employees employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            Employees hr = employeeRepository.findById(hrId)
                    .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrId));

            if (travelRepository.findByTravelPlan_TravelIdAndEmployee_EmployeeId(travelPlanId, employeeId)
                    .isPresent()) {
                throw new IllegalArgumentException("Employee is already assigned to this travel plan");
            }

            Travels travel = new Travels();
            travel.setTravelPlan(travelPlan);
            travel.setEmployee(employee);
            travel.setCreatedAt(LocalDateTime.now());
            travel.setUpdatedAt(LocalDateTime.now());
            travel.setUpdatedByEmployee(hr);

            travelRepository.save(travel);
            notificationService.sendTravelAssignmentNotification(employeeId, travelPlanId, travelPlan.getTitle());
            logger.info("Successfully added employee ID: {} to travel plan ID: {}", employeeId, travelPlanId);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error adding employee to travel plan: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error adding employee to travel plan: {}", e.getMessage());
            throw new RuntimeException("Database error while adding employee to travel plan", e);
        }
    }

    @Override
    @Transactional
    public void removeEmployeeFromTravelPlan(Long travelPlanId, Long employeeId, Long hrId) {
        logger.info("Removing employee ID: {} from travel plan ID: {} by HR ID: {}", employeeId, travelPlanId, hrId);

        try {
            Travels travel = travelRepository.findByTravelPlan_TravelIdAndEmployee_EmployeeId(travelPlanId, employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee is not assigned to this travel plan"));

            travelRepository.delete(travel);

            logger.info("Successfully removed employee ID: {} from travel plan ID: {}", employeeId, travelPlanId);

        } catch (EntityNotFoundException e) {
            logger.error("Error removing employee from travel plan: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error removing employee from travel plan: {}", e.getMessage());
            throw new RuntimeException("Database error while removing employee from travel plan", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canSubmitExpenseForTravel(Long travelPlanId, Long employeeId) {
        logger.info("Checking if employee ID: {} can submit expense for travel plan ID: {}", employeeId, travelPlanId);

        try {
            TravelPlans travelPlan = travelPlanRepository.findById(travelPlanId)
                    .orElseThrow(() -> new EntityNotFoundException("Travel plan not found with ID: " + travelPlanId));

            employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            travelRepository.findByTravelPlan_TravelIdAndEmployee_EmployeeId(travelPlanId, employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Employee is not assigned to this travel plan"));

            LocalDate today = LocalDate.now();
            LocalDate submissionDeadline = travelPlan.getEndDate().plusDays(EXPENSE_SUBMISSION_GRACE_DAYS);

            boolean canSubmit = !today.isBefore(travelPlan.getStartDate()) && !today.isAfter(submissionDeadline);

            logger.debug("Employee ID: {} can submit expense for travel plan ID: {} = {}",
                    employeeId, travelPlanId, canSubmit);

            return canSubmit;

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error checking expense submission eligibility: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error checking expense submission eligibility: {}", e.getMessage());
            throw new RuntimeException("Database error while checking expense submission eligibility", e);
        }
    }

    private TravelPlanDto toTravelPlanDto(TravelPlans travelPlan) {
        TravelPlanDto dto = new TravelPlanDto();
        dto.setTravelId(travelPlan.getTravelId());
        dto.setTitle(travelPlan.getTitle());
        dto.setDescription(travelPlan.getDescription());
        dto.setStartDate(travelPlan.getStartDate());
        dto.setEndDate(travelPlan.getEndDate());
        dto.setCreatedByHRId(travelPlan.getCreatedByHR().getEmployeeId());
        dto.setCreatedByHRName(travelPlan.getCreatedByHR().getFirstName() + " " +
                travelPlan.getCreatedByHR().getLastName());
        dto.setCreatedAt(travelPlan.getCreatedAt());
        dto.setUpdatedAt(travelPlan.getUpdatedAt());

        if (travelPlan.getUpdatedByEmployee() != null) {
            dto.setUpdatedByEmployeeId(travelPlan.getUpdatedByEmployee().getEmployeeId());
        }

        List<Travels> travels = travelRepository.findByTravelPlan_TravelId(travelPlan.getTravelId());
        List<EmployeeSummaryDto> travelers = travels.stream()
                .map(travel -> toEmployeeSummaryDto(travel.getEmployee()))
                .toList();
        dto.setTravelers(travelers);

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
}
