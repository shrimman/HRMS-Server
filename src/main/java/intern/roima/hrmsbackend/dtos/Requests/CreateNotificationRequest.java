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
public class CreateNotificationRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message cannot exceed 500 characters")
    private String message;

    @NotBlank(message = "Notification type is required")
    @Size(max = 50, message = "Type cannot exceed 50 characters")
    private String notificationType;

    private Long relatedEntityId;
}
