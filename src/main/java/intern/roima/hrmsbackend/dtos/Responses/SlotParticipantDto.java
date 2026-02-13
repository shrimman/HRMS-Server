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
public class SlotParticipantDto {
    private Long slotParticipantId;
    private Long bookingId;
    private EmployeeSummaryDto employee;
    private LocalDateTime createdAt;
}
