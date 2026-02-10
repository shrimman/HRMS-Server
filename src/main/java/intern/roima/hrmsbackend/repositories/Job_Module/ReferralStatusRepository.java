package intern.roima.hrmsbackend.repositories.Job_Module;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Job_Module.ReferralStatus;

public interface ReferralStatusRepository extends JpaRepository<ReferralStatus, Long> {
    Optional<ReferralStatus> findByStatusName(String statusName);
}
