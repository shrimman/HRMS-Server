package intern.roima.hrmsbackend.services.Employee_Module.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.exceptions.InvalidEmployeeDataException;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Employee_Module.ManagerTeamService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ManagerTeamServiceImpl implements ManagerTeamService {
    private static final Logger logger = LoggerFactory.getLogger(ManagerTeamServiceImpl.class);
    private final EmployeeRepository employeeRepository;

    public ManagerTeamServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSummaryDto> getMyTeam(@CurrentUser Long managerId) {
        logger.info("Fetching team members for manager ID: {}", managerId);

        try {
            validateManagerId(managerId);

            Employees manager = employeeRepository.findById(managerId)
                    .orElseThrow(() -> new EntityNotFoundException("Manager not found with ID: " + managerId));

            if (!manager.isActive()) {
                throw new InvalidEmployeeDataException("Manager account is inactive");
            }

            List<Employees> teamMembers = employeeRepository.findByManagerEmployeeId(managerId);
            return teamMembers.stream()
                    .filter(Employees::isActive)
                    .map(this::toSummaryDto)
                    .toList();
        } catch (EntityNotFoundException | InvalidEmployeeDataException e) {
            logger.error("Error fetching team members for manager ID {}: {}", managerId, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error fetching team members for manager ID {}: {}", managerId, e.getMessage());
            throw new RuntimeException("Database error while fetching team members", e);
        } catch (RuntimeException e) {
            logger.error("Unexpected error fetching team members for manager ID {}: {}", managerId, e.getMessage(), e);
            throw e;
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

    private void validateManagerId(Long managerId) {
        if (managerId == null || managerId <= 0) {
            throw new InvalidEmployeeDataException("Invalid manager ID: " + managerId);
        }
    }

}
