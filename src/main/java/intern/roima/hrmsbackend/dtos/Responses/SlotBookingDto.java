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
public class SlotBookingDto {
    private Long bookingId;
    private Long slotId;
    private LocalDate slotDate;
    private LocalDateTime slotStartDateTime;
    private LocalDateTime slotEndDateTime;
    private Long gameId;
    private String gameName;
    private Integer slotMaxPlayers;
    private Long bookedByEmployeeId;
    private String bookedByEmployeeName;
    private Long bookingStatusId;
    private String bookingStatusName;
    private Integer participantCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long updatedByEmployeeId;
    private List<EmployeeSummaryDto> participants;
}
