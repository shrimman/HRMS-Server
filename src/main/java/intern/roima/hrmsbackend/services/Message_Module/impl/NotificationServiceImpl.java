package intern.roima.hrmsbackend.services.Message_Module.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intern.roima.hrmsbackend.dtos.Requests.CreateNotificationRequest;
import intern.roima.hrmsbackend.dtos.Responses.NotificationDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.entities.Message_Module.Notification;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.repositories.Message_Module.NotificationRepository;
import intern.roima.hrmsbackend.services.Message_Module.NotificationService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
            EmployeeRepository employeeRepository) {
        this.notificationRepository = notificationRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public NotificationDto createNotification(CreateNotificationRequest request) {
        logger.info("Creating notification for employee ID: {}", request.getEmployeeId());

        Employees employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Employee not found with ID: " + request.getEmployeeId()));

        Notification notification = new Notification();
        notification.setEmployee(employee);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setNotificationType(request.getNotificationType());
        notification.setRelatedEntityId(request.getRelatedEntityId());
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        logger.info("Notification created with ID: {}", saved.getNotificationId());

        return toDto(saved);
    }

    @Override
    @Transactional
    public NotificationDto sendTravelAssignmentNotification(Long employeeId, Long travelPlanId, String travelTitle) {
        logger.info("Sending travel assignment notification to employee ID: {}", employeeId);

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setEmployeeId(employeeId);
        request.setTitle("New Travel Assignment");
        request.setMessage("You have been assigned to travel: " + travelTitle);
        request.setNotificationType("TRAVEL_ASSIGNMENT");
        request.setRelatedEntityId(travelPlanId);

        return createNotification(request);
    }

    @Override
    @Transactional
    public List<NotificationDto> sendTravelAssignmentNotifications(List<Long> employeeIds, Long travelPlanId,
            String travelTitle) {
        logger.info("Sending travel assignment notifications to {} employees", employeeIds.size());

        List<NotificationDto> notifications = new ArrayList<>();
        for (Long employeeId : employeeIds) {
            try {
                NotificationDto notification = sendTravelAssignmentNotification(employeeId, travelPlanId,
                        travelTitle);
                notifications.add(notification);
            } catch (EntityNotFoundException e) {
                logger.warn("Skipping notification for non-existent employee ID: {}", employeeId);
            }
        }

        return notifications;
    }

    @Override
    @Transactional
    public NotificationDto sendExpenseSubmissionNotification(Long hrId, Long expenseId, String employeeName,
            String travelTitle) {
        logger.info("Sending expense submission notification to HR ID: {}", hrId);

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setEmployeeId(hrId);
        request.setTitle("New Expense Submitted");
        request.setMessage(employeeName + " submitted an expense for travel: " + travelTitle);
        request.setNotificationType("EXPENSE_SUBMISSION");
        request.setRelatedEntityId(expenseId);

        return createNotification(request);
    }

    @Override
    @Transactional
    public NotificationDto sendExpenseStatusNotification(Long employeeId, Long expenseId, String status,
            String remarks) {
        logger.info("Sending expense status notification to employee ID: {}", employeeId);

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setEmployeeId(employeeId);
        request.setTitle("Expense Status Updated");
        request.setMessage("Your expense status: " + status + (remarks != null ? ". Remarks: " + remarks : ""));
        request.setNotificationType("EXPENSE_STATUS");
        request.setRelatedEntityId(expenseId);

        return createNotification(request);
    }

    @Override
    @Transactional
    public NotificationDto sendSlotBookingNotification(Long employeeId, Long slotId, String gameName,
            String slotDateTime) {
        logger.info("Sending slot booking notification to employee ID: {}", employeeId);

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setEmployeeId(employeeId);
        request.setTitle("Slot Booked Successfully");
        request.setMessage("Your " + gameName + " slot has been booked for " + slotDateTime);
        request.setNotificationType("SLOT_BOOKING");
        request.setRelatedEntityId(slotId);

        return createNotification(request);
    }

    @Override
    @Transactional
    public List<NotificationDto> sendSlotBookingNotifications(List<Long> employeeIds, Long slotId, String gameName,
            String slotDateTime) {
        logger.info("Sending slot booking notifications to {} employees", employeeIds.size());

        List<NotificationDto> notifications = new ArrayList<>();
        for (Long employeeId : employeeIds) {
            try {
                NotificationDto notification = sendSlotBookingNotification(employeeId, slotId, gameName,
                        slotDateTime);
                notifications.add(notification);
            } catch (EntityNotFoundException e) {
                logger.warn("Skipping notification for non-existent employee ID: {}", employeeId);
            }
        }

        return notifications;
    }

    @Override
    @Transactional
    public NotificationDto sendWarningNotification(Long employeeId, String reason, Long relatedEntityId) {
        logger.info("Sending warning notification to employee ID: {}", employeeId);

        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setEmployeeId(employeeId);
        request.setTitle("Content Warning");
        request.setMessage("Your content has been removed. Reason: " + reason);
        request.setNotificationType("WARNING");
        request.setRelatedEntityId(relatedEntityId);

        return createNotification(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getMyNotifications(Long employeeId, int page, int size) {
        logger.info("Fetching notifications for employee ID: {} (page: {}, size: {})", employeeId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        List<Notification> notifications = notificationRepository
                .findByEmployee_EmployeeIdOrderByCreatedAtDesc(employeeId, pageable);

        return notifications.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getMyUnreadNotifications(Long employeeId) {
        logger.info("Fetching unread notifications for employee ID: {}", employeeId);

        List<Notification> notifications = notificationRepository
                .findByEmployee_EmployeeIdAndIsReadOrderByCreatedAtDesc(employeeId, false);

        return notifications.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long employeeId) {
        logger.info("Counting unread notifications for employee ID: {}", employeeId);

        return notificationRepository.countByEmployee_EmployeeIdAndIsReadFalse(employeeId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long employeeId) {
        logger.info("Marking notification {} as read for employee ID: {}", notificationId, employeeId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with ID: " + notificationId));

        if (!notification.getEmployee().getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("Notification does not belong to this employee");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        logger.info("Notification {} marked as read", notificationId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long employeeId) {
        logger.info("Marking all notifications as read for employee ID: {}", employeeId);

        List<Notification> unreadNotifications = notificationRepository
                .findByEmployee_EmployeeIdAndIsReadOrderByCreatedAtDesc(employeeId, false);

        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
        }

        notificationRepository.saveAll(unreadNotifications);

        logger.info("Marked {} notifications as read for employee ID: {}", unreadNotifications.size(), employeeId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long employeeId) {
        logger.info("Deleting notification {} for employee ID: {}", notificationId, employeeId);

        notificationRepository.deleteByNotificationIdAndEmployee_EmployeeId(notificationId, employeeId);

        logger.info("Notification {} deleted successfully", notificationId);
    }

    private NotificationDto toDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setNotificationId(notification.getNotificationId());
        dto.setEmployeeId(notification.getEmployee().getEmployeeId());
        dto.setEmployeeName(notification.getEmployee().getFirstName() + " " + notification.getEmployee().getLastName());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setNotificationType(notification.getNotificationType());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
