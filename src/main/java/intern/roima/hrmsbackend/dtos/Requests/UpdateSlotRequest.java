package intern.roima.hrmsbackend.dtos.Requests;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSlotRequest {


    @Min(value = 1, message = "Max players must be at least 1")
    private Integer maxPlayers;

    private Long slotStatusId;
}
