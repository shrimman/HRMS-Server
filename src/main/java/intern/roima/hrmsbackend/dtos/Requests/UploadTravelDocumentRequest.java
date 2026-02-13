package intern.roima.hrmsbackend.dtos.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadTravelDocumentRequest {

    @NotNull(message = "Travel plan ID is required")
    private Long travelPlanId;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Document type ID is required")
    private Long documentTypeId;

    @NotBlank(message = "Document name is required")
    @Size(max = 255, message = "Document name cannot exceed 255 characters")
    private String documentName;
}
