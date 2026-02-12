package intern.roima.hrmsbackend.services.Job_Module;

import java.util.List;

import intern.roima.hrmsbackend.dtos.Requests.ShareJobRequest;
import intern.roima.hrmsbackend.dtos.Responses.JobShareLogDto;

public interface JobShareService {

    List<JobShareLogDto> shareJob(Long jobId, ShareJobRequest shareRequest, Long employeeId);

    List<JobShareLogDto> getShareLogsForJob(Long jobId);

    List<JobShareLogDto> getMyShareHistory(Long employeeId);
}
