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
public class JobOpeningDto {
    private Long jobId;
    private String title;
    private String summary;
    private String jdFilePath;
    private Boolean isActive;
    private Long jobHROwnerId;
    private LocalDateTime postedAt;
    private LocalDateTime updatedAt;
    private EmployeeSummaryDto updatedByEmployee;
}
