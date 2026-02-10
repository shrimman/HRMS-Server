package intern.roima.hrmsbackend.repositories.Employee_Module;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import intern.roima.hrmsbackend.entities.Employee_Module.Employees;

@Repository
public interface EmployeeRepository extends JpaRepository<Employees, Long> {
    Optional<Employees> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Employees> findByManagerEmployeeId(Long managerId);

}
