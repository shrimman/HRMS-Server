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
public class JobReviewerDto {
    private Long reviewerId;
    private JobOpeningDto jobOpening;
    private EmployeeSummaryDto reviewer;
    private EmployeeSummaryDto assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
    private EmployeeSummaryDto updatedByEmployee;
}
