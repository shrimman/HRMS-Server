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
public class NotificationDto {
    private Long notificationId;
    private Long employeeId;
    private String employeeName;
    private String title;
    private String message;
    private String notificationType;
    private Long relatedEntityId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
