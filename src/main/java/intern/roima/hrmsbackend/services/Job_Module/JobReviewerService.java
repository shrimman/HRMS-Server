package intern.roima.hrmsbackend.services.Job_Module;

import java.util.List;

import intern.roima.hrmsbackend.dtos.Responses.JobOpeningDto;
import intern.roima.hrmsbackend.dtos.Responses.JobReviewerDto;

public interface JobReviewerService {

    JobReviewerDto assignReviewer(Long jobId, Long reviewerId, Long assignedBy);

    void removeReviewer(Long jobReviewerId, Long hrId);

    List<JobReviewerDto> getReviewersForJob(Long jobId);

    List<JobOpeningDto> getJobsForReviewer(Long reviewerId);
}
