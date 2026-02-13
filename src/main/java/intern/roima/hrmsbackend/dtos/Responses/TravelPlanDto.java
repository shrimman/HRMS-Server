package intern.roima.hrmsbackend.dtos.Responses;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TravelPlanDto {
    private Long travelId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long createdByHRId;
    private String createdByHRName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long updatedByEmployeeId;
    private List<EmployeeSummaryDto> travelers;
}
