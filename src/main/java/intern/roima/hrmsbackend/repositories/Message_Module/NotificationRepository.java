package intern.roima.hrmsbackend.repositories.Message_Module;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Message_Module.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
