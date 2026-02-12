package intern.roima.hrmsbackend.services.Job_Module;

import java.util.List;

import intern.roima.hrmsbackend.dtos.Requests.CreateJobRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateJobRequest;
import intern.roima.hrmsbackend.dtos.Responses.JobOpeningDto;

public interface JobOpeningService {

    List<JobOpeningDto> getAllActiveJobs();

    List<JobOpeningDto> getAllJobs();

    JobOpeningDto getJobById(Long jobId);

    JobOpeningDto createJob(CreateJobRequest jobRequest, Long hrId);

    JobOpeningDto updateJob(Long jobId, UpdateJobRequest jobRequest, Long hrId);

    void deactivateJob(Long jobId, Long hrId);

    void activateJob(Long jobId, Long hrId);

}
