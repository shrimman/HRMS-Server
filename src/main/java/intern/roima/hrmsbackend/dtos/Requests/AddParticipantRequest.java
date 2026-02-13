package intern.roima.hrmsbackend.dtos.Requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddParticipantRequest {

    @NotNull(message = "Participant employee ID is required")
    private Long participantEmployeeId;
}
