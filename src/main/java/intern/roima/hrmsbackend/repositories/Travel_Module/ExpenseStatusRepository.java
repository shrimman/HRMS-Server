package intern.roima.hrmsbackend.repositories.Travel_Module;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Travel_Module.ExpenseStatus;

public interface ExpenseStatusRepository extends JpaRepository<ExpenseStatus, Long> {
    Optional<ExpenseStatus> findByStatusName(String statusName);
}
