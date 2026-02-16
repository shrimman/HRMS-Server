package intern.roima.hrmsbackend.services.Employee_Module.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.OrgChartResponseDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Departments;
import intern.roima.hrmsbackend.entities.Employee_Module.Designations;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.exceptions.InvalidEmployeeDataException;
import intern.roima.hrmsbackend.repositories.Employee_Module.DepartmentRepository;
import intern.roima.hrmsbackend.repositories.Employee_Module.DesignationRepository;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.services.Employee_Module.EmployeeDirectoryService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class EmployeeDirectoryImpl implements EmployeeDirectoryService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeDirectoryImpl.class);
    private static final int MAX_MANAGER_CHAIN_DEPTH = 20;

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;

    public EmployeeDirectoryImpl(EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            DesignationRepository designationRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public OrgChartResponseDto getOrgChart(Long employeeId) {
        logger.info("Building org chart for employee ID: {}", employeeId);

        try {
            Employees targetEmployee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            List<EmployeeSummaryDto> managerChain = buildManagerChain(targetEmployee);

            List<EmployeeSummaryDto> directReports = employeeRepository
                    .findByManagerEmployeeId(employeeId)
                    .stream()
                    .filter(Employees::isActive)
                    .map(this::toSummaryDto)
                    .toList();

            OrgChartResponseDto response = new OrgChartResponseDto();
            response.setSelectedEmployee(toSummaryDto(targetEmployee));
            response.setManagerChain(managerChain);
            response.setDirectReports(directReports);

            logger.debug("Successfully built org chart for employee ID: {} with {} managers and {} direct reports",
                    employeeId, managerChain.size(), directReports.size());

            return response;

        } catch (EntityNotFoundException | InvalidEmployeeDataException e) {
            logger.error("Error building org chart for employee ID {}: {}", employeeId, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error building org chart for employee ID {}: {}", employeeId, e.getMessage());
            throw new RuntimeException("Database error while building org chart", e);
        }
    }

    private List<EmployeeSummaryDto> buildManagerChain(Employees employee) {
        List<EmployeeSummaryDto> chain = new ArrayList<>();
        Set<Long> visitedIds = new HashSet<>();

        Employees current = employee.getManager();
        int depth = 0;

        while (current != null) {
            depth++;

            if (depth > MAX_MANAGER_CHAIN_DEPTH) {
                logger.error("Manager chain depth exceeded maximum ({}) for employee ID: {}",
                        MAX_MANAGER_CHAIN_DEPTH, employee.getEmployeeId());
                throw new InvalidEmployeeDataException("Manager chain depth exceeds maximum allowed depth.");
            }

            if (visitedIds.contains(current.getEmployeeId())) {
                logger.error("Circular reference detected in manager chain for employee ID: {}",
                        employee.getEmployeeId());
                throw new InvalidEmployeeDataException(
                        "Circular reference detected in manager hierarchy");
            }

            visitedIds.add(current.getEmployeeId());
            chain.add(toSummaryDto(current));
            current = current.getManager();
        }

        Collections.reverse(chain);
        return chain;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSummaryDto> getAllEmployees() {
        logger.info("Fetching all active employees");

        try {
            List<EmployeeSummaryDto> employees = employeeRepository
                    .findByIsActive(true)
                    .stream()
                    .map(this::toSummaryDto)
                    .toList();

            logger.debug("Successfully fetched {} active employees", employees.size());
            return employees;

        } catch (DataAccessException e) {
            logger.error("Database error fetching all employees: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching employees", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSummaryDto> searchEmployees(
            Long currentUserId,
            String query,
            String department,
            String designation,
            String role) {

        logger.info("Searching employees with query: '{}', department: '{}', designation: '{}', role: '{}'",
                query, department, designation, role);

        try {
            List<Employees> employees = employeeRepository.searchEmployees(
                    query,
                    department,
                    designation,
                    role);

            logger.info("Repository returned {} employees", employees.size());

            List<EmployeeSummaryDto> results = employees
                    .stream()
                    .map(this::toSummaryDto)
                    .toList();

            logger.info("Search returned {} results", results.size());

            return results;

        } catch (IllegalArgumentException e) {
            logger.error("Invalid search parameters: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error searching employees: {}", e.getMessage());
            throw new RuntimeException("Database error while searching employees", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSummaryDto> getEmployeesByDepartment(String department) {
        logger.info("Fetching employees by department: {}", department);

        try {
            List<EmployeeSummaryDto> employees = employeeRepository
                    .findByDepartmentName(department)
                    .stream()
                    .map(this::toSummaryDto)
                    .toList();

            logger.debug("Found {} employees in department: {}", employees.size(), department);
            return employees;

        } catch (DataAccessException e) {
            logger.error("Database error fetching employees by department: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching employees by department", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSummaryDto> getEmployeesByDesignation(String designation) {
        logger.info("Fetching employees by designation: {}", designation);

        try {
            List<EmployeeSummaryDto> employees = employeeRepository
                    .findByDesignationName(designation)
                    .stream()
                    .map(this::toSummaryDto)
                    .toList();

            logger.debug("Found {} employees with designation: {}", employees.size(), designation);
            return employees;

        } catch (DataAccessException e) {
            logger.error("Database error fetching employees by designation: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching employees by designation", e);
        }
    }

    private EmployeeSummaryDto toSummaryDto(Employees employee) {
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
    @Transactional(readOnly = true)
    public List<String> getAllDepartments() {
        logger.info("Fetching all department names");

        try {
            List<String> departmentNames = departmentRepository.findAll()
                    .stream()
                    .map(Departments::getDepartmentName)
                    .toList();
            logger.debug("Successfully fetched {} department names", departmentNames.size());
            return departmentNames;

        } catch (DataAccessException e) {
            logger.error("Database error fetching all departments: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching departments", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllDesignations() {
        logger.info("Fetching all designation names");

        try {
            List<String> designationNames = designationRepository.findAll()
                    .stream()
                    .map(Designations::getDesignationName)
                    .toList();
            logger.debug("Successfully fetched {} designation names", designationNames.size());
            return designationNames;

        } catch (DataAccessException e) {
            logger.error("Database error fetching all designations: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching designations", e);
        }
    }
}
