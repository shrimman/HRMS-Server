package intern.roima.hrmsbackend.services.Message_Module;

import java.util.List;

import intern.roima.hrmsbackend.dtos.Requests.CreateNotificationRequest;
import intern.roima.hrmsbackend.dtos.Responses.NotificationDto;

public interface NotificationService {

        NotificationDto createNotification(CreateNotificationRequest request);

        NotificationDto sendTravelAssignmentNotification(Long employeeId, Long travelPlanId, String travelTitle);

        List<NotificationDto> sendTravelAssignmentNotifications(List<Long> employeeIds, Long travelPlanId, String travelTitle);

        NotificationDto sendExpenseSubmissionNotification(Long hrId, Long expenseId, String employeeName, String travelTitle);

        NotificationDto sendExpenseStatusNotification(Long employeeId, Long expenseId, String status, String remarks);

        NotificationDto sendSlotBookingNotification(Long employeeId, Long slotId, String gameName, String slotDateTime);

        List<NotificationDto> sendSlotBookingNotifications(List<Long> employeeIds, Long slotId, String gameName, String slotDateTime);

        NotificationDto sendWarningNotification(Long employeeId, String reason, Long relatedEntityId);

        List<NotificationDto> getMyNotifications(Long employeeId, int page, int size);

        List<NotificationDto> getMyUnreadNotifications(Long employeeId);

        Long getUnreadCount(Long employeeId);

        void markAsRead(Long notificationId, Long employeeId);

        void markAllAsRead(Long employeeId);

        void deleteNotification(Long notificationId, Long employeeId);

}
