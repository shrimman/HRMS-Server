package intern.roima.hrmsbackend.repositories.Employee_Module;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Employee_Module.Departments;

public interface DepartmentRepository extends JpaRepository<Departments, Long> {
}
