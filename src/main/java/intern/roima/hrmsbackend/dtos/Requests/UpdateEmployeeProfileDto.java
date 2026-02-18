package intern.roima.hrmsbackend.dtos.Requests;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeProfileDto {

    private LocalDate dateOfJoining;

    private Long managerId;

    private Long departmentId;

    private Long designationId;

    private Integer roleId;

    private Boolean isActive;
}
