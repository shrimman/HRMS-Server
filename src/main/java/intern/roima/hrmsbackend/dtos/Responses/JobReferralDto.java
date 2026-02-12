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
public class JobReferralDto {
    public Long referralId;
    public JobOpeningDto jobOpening;
    public EmployeeSummaryDto referrer;
    public String friendName;
    public String friendEmail;
    public String cvFilePath;
    public String note;
    public ReferralStatusDto referralStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private EmployeeSummaryDto updatedByEmployee;

}
