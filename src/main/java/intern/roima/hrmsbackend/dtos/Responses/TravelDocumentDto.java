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
public class TravelDocumentDto {
    private Long documentId;
    private Long travelPlanId;
    private String travelPlanTitle;
    private Long employeeId;
    private String employeeName;
    private Long documentTypeId;
    private String documentTypeName;
    private String documentName;
    private String documentPath;
    private LocalDateTime uploadedAt;
    private Long uploadedById;
    private String uploadedByName;
}
