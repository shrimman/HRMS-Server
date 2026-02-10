package intern.roima.hrmsbackend.repositories.Employee_Module;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Employee_Module.Designations;

public interface DesignationRepository extends JpaRepository<Designations, Long> {

    Optional<Designations> findByDesignationName(String designation);

}
