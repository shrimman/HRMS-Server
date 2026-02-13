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
public class GameDto {
    private Long gameId;
    private String gameName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long updatedByEmployeeId;
}
