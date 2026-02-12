package intern.roima.hrmsbackend.controllers.Job;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import intern.roima.hrmsbackend.dtos.Responses.JobOpeningDto;
import intern.roima.hrmsbackend.dtos.Responses.JobReviewerDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Job_Module.JobReviewerService;

@RestController
@RequestMapping("/api/reviewers")
public class JobReviewerController {

    private final JobReviewerService jobReviewerService;

    public JobReviewerController(JobReviewerService jobReviewerService) {
        this.jobReviewerService = jobReviewerService;
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobReviewerDto> assignReviewer(
            @RequestParam("jobId") Long jobId, @RequestParam("reviewerId") Long reviewerId,
            @CurrentUser Long assignedBy) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobReviewerService.assignReviewer(jobId, reviewerId, assignedBy));
    }

    @DeleteMapping("/{jobReviewerId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> removeReviewer(
            @PathVariable("jobReviewerId") Long jobReviewerId,
            @CurrentUser Long hrId) {
        jobReviewerService.removeReviewer(jobReviewerId, hrId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<JobReviewerDto>> getReviewersForJob(@PathVariable("jobId") Long jobId) {
        return ResponseEntity.ok(jobReviewerService.getReviewersForJob(jobId));
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<JobOpeningDto>> getJobsForReviewer(@CurrentUser Long reviewerId) {
        return ResponseEntity.ok(jobReviewerService.getJobsForReviewer(reviewerId));
    }
}
