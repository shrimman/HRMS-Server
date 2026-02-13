package intern.roima.hrmsbackend.dtos.Responses;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReferralStatusDto {
    private Long statusId;
    private String statusName;
    private LocalDateTime updatedAt;
    private EmployeeSummaryDto updatedByEmployee;
}
