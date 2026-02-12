package intern.roima.hrmsbackend.repositories.Job_Module;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Job_Module.JobOpenings;

public interface JobOpeningRepository extends JpaRepository<JobOpenings, Long> {
    List<JobOpenings> findByIsActive(Boolean isActive);

    List<JobOpenings> findByIsActiveTrue();
}
