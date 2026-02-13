package intern.roima.hrmsbackend.dtos.Requests;

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
public class UpdateTravelDocumentRequest {

    @NotNull(message = "Document type ID is required")
    private Long documentTypeId;

    @Size(max = 255, message = "Document name cannot exceed 255 characters")
    private String documentName;
}
