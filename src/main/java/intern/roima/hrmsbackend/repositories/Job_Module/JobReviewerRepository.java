package intern.roima.hrmsbackend.repositories.Job_Module;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Job_Module.JobReviewers;

public interface JobReviewerRepository extends JpaRepository<JobReviewers, Long> {
    List<JobReviewers> findByJobOpening_JobId(Long jobId);
    
    List<JobReviewers> findByReviewer_EmployeeId(Long employeeId);
}
