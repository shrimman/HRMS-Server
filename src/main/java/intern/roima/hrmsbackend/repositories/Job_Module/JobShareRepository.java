package intern.roima.hrmsbackend.repositories.Job_Module;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Job_Module.JobShareLog;

public interface JobShareRepository extends JpaRepository<JobShareLog, Long> {
    List<JobShareLog> findByJobOpening_JobId(Long jobId);
    
    List<JobShareLog> findBySharedBy_EmployeeId(Long employeeId);
}
