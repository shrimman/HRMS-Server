package intern.roima.hrmsbackend.repositories.Travel_Module;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Travel_Module.ExpenseType;

public interface ExpenseTypeRepository extends JpaRepository<ExpenseType, Long> {
    Optional<ExpenseType> findByTypeName(String typeName);
}
