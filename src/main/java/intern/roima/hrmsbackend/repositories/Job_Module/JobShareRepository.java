package intern.roima.hrmsbackend.repositories.Job_Module;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Job_Module.JobShareLog;

public interface JobShareRepository extends JpaRepository<JobShareLog, Long> {

}
