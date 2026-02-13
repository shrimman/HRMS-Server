package intern.roima.hrmsbackend.controllers.Notification;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import intern.roima.hrmsbackend.dtos.Responses.NotificationDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Message_Module.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<NotificationDto>> getMyNotifications(
             @RequestParam("page") int page, 
             @RequestParam("size") int size,
             @CurrentUser Long employeeId) {
        return ResponseEntity.ok(notificationService.getMyNotifications(employeeId, page, size));
    }

    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<NotificationDto>> getUnreadNotifications(
            @CurrentUser Long employeeId) {
        return ResponseEntity.ok(notificationService.getMyUnreadNotifications(employeeId));
    }

    @GetMapping("/unread/count")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Long> getUnreadCount(@CurrentUser Long employeeId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(employeeId));
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id, @CurrentUser Long employeeId) {
        notificationService.markAsRead(id, employeeId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> markAllAsRead(@CurrentUser Long employeeId) {
        notificationService.markAllAsRead(employeeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> deleteNotification(@PathVariable("id") Long id, @CurrentUser Long employeeId) {
        notificationService.deleteNotification(id, employeeId);
        return ResponseEntity.ok().build();
    }
}
