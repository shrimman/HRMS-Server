package intern.roima.hrmsbackend.dtos.Requests;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookSlotRequest {

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotNull(message = "Participant IDs are required")
    private List<Long> participantIds;
}
