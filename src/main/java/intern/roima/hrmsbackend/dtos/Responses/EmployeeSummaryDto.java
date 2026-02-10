package intern.roima.hrmsbackend.dtos.Responses;

import java.time.LocalDate;

import intern.roima.hrmsbackend.entities.Employee_Module.Departments;
import intern.roima.hrmsbackend.entities.Employee_Module.Designations;
import intern.roima.hrmsbackend.entities.Employee_Module.Roles;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSummaryDto {
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String email;
    private Roles role;
    private Long managerId;
    private String managerName;
    private Departments department;
    private Designations designation;
    private boolean isActive;
    private String photoPath;
    private LocalDate dateOfBirth;
    private LocalDate dateOfJoining;
}
