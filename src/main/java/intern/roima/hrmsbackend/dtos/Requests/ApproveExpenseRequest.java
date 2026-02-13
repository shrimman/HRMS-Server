package intern.roima.hrmsbackend.dtos.Requests;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApproveExpenseRequest {

    @Size(max = 255, message = "Remarks cannot exceed 255 characters")
    private String remarks;
}
