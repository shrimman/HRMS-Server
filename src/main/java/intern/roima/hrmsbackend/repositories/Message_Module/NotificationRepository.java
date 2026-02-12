package intern.roima.hrmsbackend.repositories.Message_Module;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Message_Module.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByEmployee_EmployeeIdOrderByCreatedAtDesc(Long employeeId, Pageable pageable);

    List<Notification> findByEmployee_EmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<Notification> findByEmployee_EmployeeIdAndIsReadOrderByCreatedAtDesc(Long employeeId, Boolean isRead);

    Long countByEmployee_EmployeeIdAndIsReadFalse(Long employeeId);

    void deleteByNotificationIdAndEmployee_EmployeeId(Long notificationId, Long employeeId);

}
