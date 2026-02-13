package intern.roima.hrmsbackend.dtos.Responses;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameSlotDto {
    private Long slotId;
    private Long gameId;
    private String gameName;
    private LocalDate slotDate;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer maxPlayers;
    private Integer currentParticipants;
    private Integer availableSpots;
    private Long slotStatusId;
    private String slotStatusName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long updatedByEmployeeId;
}
