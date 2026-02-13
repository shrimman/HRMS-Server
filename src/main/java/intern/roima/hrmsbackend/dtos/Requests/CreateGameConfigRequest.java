package intern.roima.hrmsbackend.dtos.Requests;

import java.time.LocalTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateGameConfigRequest {

    @NotNull(message = "Game ID is required")
    private Long gameId;

    @NotNull(message = "Game duration is required")
    @Min(value = 1, message = "Game duration must be at least 1 minute")
    private Integer gameDuration;

    @NotNull(message = "Max players is required")
    @Min(value = 1, message = "Max players must be at least 1")
    private Integer maxPlayers;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;
}
