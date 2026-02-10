package intern.roima.hrmsbackend.repositories.Game_Module;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Game_Module.SlotStatus;

public interface SlotStatusRepository extends JpaRepository<SlotStatus, Long> {
    Optional<SlotStatus> findByStatusName(String statusName);
}
