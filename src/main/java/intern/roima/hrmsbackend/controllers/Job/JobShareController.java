package intern.roima.hrmsbackend.controllers.Job;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import intern.roima.hrmsbackend.dtos.Requests.ShareJobRequest;
import intern.roima.hrmsbackend.dtos.Responses.JobShareLogDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Job_Module.JobShareService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/jobs/share")
public class JobShareController {

    private final JobShareService jobShareService;

    public JobShareController(JobShareService jobShareService) {
        this.jobShareService = jobShareService;
    }

    @PostMapping("/{jobId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<JobShareLogDto>> shareJob(
            @PathVariable("jobId") Long jobId,
            @Valid @RequestBody ShareJobRequest shareRequest,
            @CurrentUser Long employeeId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobShareService.shareJob(jobId, shareRequest, employeeId));
    }

    @GetMapping("/job/{jobId}/logs")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<JobShareLogDto>> getShareLogsForJob(@PathVariable("jobId") Long jobId) {
        return ResponseEntity.ok(jobShareService.getShareLogsForJob(jobId));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<JobShareLogDto>> getMyShareHistory(@CurrentUser Long employeeId) {
        return ResponseEntity.ok(jobShareService.getMyShareHistory(employeeId));
    }
}
