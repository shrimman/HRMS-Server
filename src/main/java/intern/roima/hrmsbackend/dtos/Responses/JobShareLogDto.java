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
public class JobShareLogDto {
    private Long shareId;
    private JobOpeningDto jobOpening;
    private EmployeeSummaryDto sharedBy;
    private String recipientEmail;
    private LocalDateTime sharedAt;
}
