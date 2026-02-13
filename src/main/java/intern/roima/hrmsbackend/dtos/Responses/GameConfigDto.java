package intern.roima.hrmsbackend.dtos.Responses;

import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameConfigDto {
    private Long configId;
    private Long gameId;
    private String gameName;
    private Integer gameDuration;
    private Integer maxPlayers;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long updatedByEmployeeId;
}
