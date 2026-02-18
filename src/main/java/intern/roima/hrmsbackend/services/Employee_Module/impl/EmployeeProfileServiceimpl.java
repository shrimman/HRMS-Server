package intern.roima.hrmsbackend.services.Employee_Module.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import intern.roima.hrmsbackend.dtos.Requests.UpdateEmployeeDto;
import intern.roima.hrmsbackend.dtos.Requests.UpdateEmployeeProfileDto;
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
import intern.roima.hrmsbackend.services.Utils.FileStorageService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class EmployeeProfileServiceimpl implements EmployeeProfileService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeProfileServiceimpl.class);

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final FileStorageService fileStorageService;

    public EmployeeProfileServiceimpl(
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            DepartmentRepository departmentRepository,
            DesignationRepository designationRepository,
            FileStorageService fileStorageService) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
        this.fileStorageService = fileStorageService;
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
    public EmployeeSummaryDto updateMyProfile(@CurrentUser Long myEmployeeId, UpdateEmployeeDto updatedProfile) {
        logger.info("Updating profile for employee ID: {}", myEmployeeId);

        try {
            if (updatedProfile == null) {
                throw new InvalidEmployeeDataException("Profile update data cannot be null");
            }

            Employees existingEmployee = employeeRepository.findById(myEmployeeId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Employee not found with ID: " + myEmployeeId));

            updateEmployeeFromUpdateDto(existingEmployee, updatedProfile);

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
    public EmployeeSummaryDto updateEmployeeProfile(Long employeeId, UpdateEmployeeProfileDto updatedProfile) {
        logger.info("Updating employee profile for employee ID: {}", employeeId);

        try {
            if (updatedProfile == null) {
                throw new InvalidEmployeeDataException("Profile update data cannot be null");
            }

            Employees existingEmployee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            if (updatedProfile.getRoleId() != null) {
                Roles role = roleRepository.findById(updatedProfile.getRoleId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Role not found with ID: " + updatedProfile.getRoleId()));
                existingEmployee.setRole(role);
            }

            if (updatedProfile.getDepartmentId() != null) {
                Departments department = departmentRepository.findById(updatedProfile.getDepartmentId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Department not found with ID: " + updatedProfile.getDepartmentId()));
                existingEmployee.setDepartment(department);
            }

            if (updatedProfile.getDesignationId() != null) {
                Designations designation = designationRepository.findById(updatedProfile.getDesignationId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Designation not found with ID: " + updatedProfile.getDesignationId()));
                existingEmployee.setDesignation(designation);
            }

            if (updatedProfile.getManagerId() != null) {
                Employees manager = employeeRepository.findById(updatedProfile.getManagerId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Manager not found with ID: " + updatedProfile.getManagerId()));
                existingEmployee.setManager(manager);
            }

            if (updatedProfile.getDateOfJoining() != null) {
                existingEmployee.setDateOfJoining(updatedProfile.getDateOfJoining());
            }

            if (updatedProfile.getIsActive() != null) {
                existingEmployee.setActive(updatedProfile.getIsActive());
            }

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

    private void updateEmployeeFromUpdateDto(Employees employee, UpdateEmployeeDto dto) {
        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            employee.setFirstName(dto.getFirstName().trim());
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            employee.setLastName(dto.getLastName().trim());
        }
        if (dto.getDateOfBirth() != null) {
            employee.setDateOfBirth(dto.getDateOfBirth());
        }
    }

    @Override
    @Transactional
    public EmployeeSummaryDto uploadProfilePhoto(Long employeeId, MultipartFile file) {
        logger.info("Uploading profile photo for employee ID: {}", employeeId);

        try {
            Employees employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Employee not found with ID: " + employeeId));

            String currentPhotoPath = employee.getPhotoPath();
            if (currentPhotoPath != null && !currentPhotoPath.isEmpty()
                    && !currentPhotoPath.startsWith("http")
                    && fileStorageService.fileExists(currentPhotoPath)) {
                logger.debug("Deleting old profile photo: {}", currentPhotoPath);
                fileStorageService.deleteFile(currentPhotoPath);
            }

            String newPhotoPath = fileStorageService.storeProfilePhoto(file, employeeId);
            logger.debug("New profile photo stored at: {}", newPhotoPath);

            employee.setPhotoPath(newPhotoPath);
            Employees savedEmployee = employeeRepository.save(employee);

            logger.info("Successfully uploaded profile photo for employee ID: {}", employeeId);
            return toSummaryDto(savedEmployee);

        } catch (EntityNotFoundException e) {
            logger.error("Error uploading profile photo for employee ID {}: {}", employeeId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error uploading profile photo for employee ID {}: {}", employeeId, e.getMessage());
            throw new RuntimeException("Failed to upload profile photo", e);
        }
    }

    @Override
    @Transactional
    public EmployeeSummaryDto deleteProfilePhoto(Long employeeId) {
        logger.info("Deleting profile photo for employee ID: {}", employeeId);

        try {
            Employees employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Employee not found with ID: " + employeeId));

            String currentPhotoPath = employee.getPhotoPath();

            if (currentPhotoPath != null && !currentPhotoPath.isEmpty()
                    && !currentPhotoPath.startsWith("http")) {
                if (fileStorageService.fileExists(currentPhotoPath)) {
                    logger.debug("Deleting profile photo: {}", currentPhotoPath);
                    fileStorageService.deleteFile(currentPhotoPath);
                }

                employee.setPhotoPath(
                        "https://pngtree.com/freepng/male-company-employee-avatar-icon-wearing-a-necktie_8537621.html");
                Employees savedEmployee = employeeRepository.save(employee);

                logger.info("Successfully deleted profile photo for employee ID: {}", employeeId);
                return toSummaryDto(savedEmployee);
            } else {
                logger.info("No custom profile photo to delete for employee ID: {}", employeeId);
                return toSummaryDto(employee);
            }

        } catch (EntityNotFoundException e) {
            logger.error("Error deleting profile photo for employee ID {}: {}", employeeId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error deleting profile photo for employee ID {}: {}", employeeId, e.getMessage());
            throw new RuntimeException("Failed to delete profile photo", e);
        }
    }

}
