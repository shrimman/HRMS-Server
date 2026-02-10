package intern.roima.hrmsbackend.services.Employee_Module.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Departments;
import intern.roima.hrmsbackend.entities.Employee_Module.Designations;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.entities.Employee_Module.Roles;
import intern.roima.hrmsbackend.exceptions.InvalidEmployeeDataException;
import intern.roima.hrmsbackend.repositories.Employee_Module.DepartmentRepository;
import intern.roima.hrmsbackend.repositories.Employee_Module.DesignationRepository;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.repositories.Employee_Module.RoleRepository;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Employee_Module.EmployeeProfileService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class EmployeeProfileServiceimpl implements EmployeeProfileService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeProfileServiceimpl.class);

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;

    public EmployeeProfileServiceimpl(
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            DepartmentRepository departmentRepository,
            DesignationRepository designationRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeSummaryDto getMyProfile(@CurrentUser Long myEmployeeId) {
        logger.info("Fetching profile for employee ID: {}", myEmployeeId);

        try {
            Employees myProfile = employeeRepository.findById(myEmployeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + myEmployeeId));

            logger.debug("Successfully fetched profile for employee ID: {}", myEmployeeId);
            return toSummaryDto(myProfile);

        } catch (EntityNotFoundException e) {
            logger.error("Error fetching profile for employee ID {}: {}", myEmployeeId, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error fetching profile for employee ID {}: {}", myEmployeeId, e.getMessage());
            throw new RuntimeException("Database error while fetching employee profile", e);
        }
    }

    @Override
    @Transactional
    public EmployeeSummaryDto updateMyProfile(@CurrentUser Long myEmployeeId, EmployeeSummaryDto updatedProfile) {
        logger.info("Updating profile for employee ID: {}", myEmployeeId);

        try {
            if (updatedProfile == null) {
                throw new InvalidEmployeeDataException("Profile update data cannot be null");
            }

            Employees existingEmployee = employeeRepository.findById(myEmployeeId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Employee not found with ID: " + myEmployeeId));

            if (updatedProfile.getEmail() != null &&
                    !existingEmployee.getEmail().equals(updatedProfile.getEmail())) {
                logger.warn("Employee {} attempted to change email from {} to {}",
                        myEmployeeId, existingEmployee.getEmail(), updatedProfile.getEmail());
                throw new InvalidEmployeeDataException("Email cannot be changed through profile update");
            }

            updateEmployeeFromDto(existingEmployee, updatedProfile);

            Employees savedEmployee = employeeRepository.save(existingEmployee);
            logger.info("Successfully updated profile for employee ID: {}", myEmployeeId);

            return toSummaryDto(savedEmployee);

        } catch (EntityNotFoundException | InvalidEmployeeDataException e) {
            logger.error("Error updating profile for employee ID {}: {}", myEmployeeId, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error updating profile for employee ID {}: {}", myEmployeeId, e.getMessage());
            throw new RuntimeException("Database error while updating employee profile", e);
        }
    }

    @Override
    @Transactional
    public EmployeeSummaryDto updateEmployeeProfile(Long employeeId, EmployeeSummaryDto updatedProfile) {
        logger.info("Updating employee profile for employee ID: {}", employeeId);

        try {
            Employees existingEmployee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            if (updatedProfile.getRole() != null && updatedProfile.getRole().getRoleId() != null) {
                Roles role = roleRepository.findById(updatedProfile.getRole().getRoleId().intValue())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Role not found with ID: " + updatedProfile.getRole().getRoleId()));
                existingEmployee.setRole(role);
            }

            if (updatedProfile.getDepartment() != null && updatedProfile.getDepartment().getDepartmentId() != null) {
                Departments department = departmentRepository.findById(updatedProfile.getDepartment().getDepartmentId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Department not found with ID: " + updatedProfile.getDepartment().getDepartmentId()));
                existingEmployee.setDepartment(department);
            }

            if (updatedProfile.getDesignation() != null && updatedProfile.getDesignation().getDesignationId() != null) {
                Designations designation = designationRepository
                        .findById(updatedProfile.getDesignation().getDesignationId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Designation not found with ID: "
                                        + updatedProfile.getDesignation().getDesignationId()));
                existingEmployee.setDesignation(designation);
            }

            existingEmployee.setActive(updatedProfile.isActive());

            Employees savedEmployee = employeeRepository.save(existingEmployee);
            logger.info("Successfully updated employee profile for employee ID: {}", employeeId);

            return toSummaryDto(savedEmployee);

        } catch (EntityNotFoundException | InvalidEmployeeDataException e) {
            logger.error("Error updating employee profile for employee ID {}: {}", employeeId, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error updating employee profile for employee ID {}: {}", employeeId, e.getMessage());
            throw new RuntimeException("Database error while updating employee profile", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeSummaryDto getEmployeeProfileById(Long employeeId) {
        logger.info("Fetching employee profile for employee ID: {}", employeeId);

        try {
            Employees targetEmployee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Employee not found with ID: " + employeeId));

            logger.debug("Successfully fetched employee profile for employee ID: {}", employeeId);
            return toSummaryDto(targetEmployee);

        } catch (EntityNotFoundException e) {
            logger.error("Error fetching employee profile for employee ID {}: {}", employeeId, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error fetching employee profile for employee ID {}: {}", employeeId, e.getMessage());
            throw new RuntimeException("Database error while fetching employee profile", e);
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

    private void updateEmployeeFromDto(Employees employee, EmployeeSummaryDto dto) {
        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            employee.setFirstName(dto.getFirstName().trim());
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            employee.setLastName(dto.getLastName().trim());
        }
        if (dto.getPhotoPath() != null && !dto.getPhotoPath().isBlank()) {
            employee.setPhotoPath(dto.getPhotoPath().trim());
        }
    }

}
