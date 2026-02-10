package intern.roima.hrmsbackend.repositories.Game_Module;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Game_Module.BookingStatus;

public interface BookingStatusRepository extends JpaRepository<BookingStatus, Long> {
    Optional<BookingStatus> findByStatusName(String statusName);
}
