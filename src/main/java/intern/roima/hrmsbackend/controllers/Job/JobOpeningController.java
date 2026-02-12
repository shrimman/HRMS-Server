package intern.roima.hrmsbackend.controllers.Job;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import intern.roima.hrmsbackend.dtos.Requests.CreateJobRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateJobRequest;
import intern.roima.hrmsbackend.dtos.Responses.JobOpeningDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Job_Module.JobOpeningService;

@RestController
@RequestMapping("/api/jobs")
public class JobOpeningController {

    private final JobOpeningService jobOpeningService;

    public JobOpeningController(JobOpeningService jobOpeningService) {
        this.jobOpeningService = jobOpeningService;
    }

    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<JobOpeningDto>> getAllJobs() {
        return ResponseEntity.ok(jobOpeningService.getAllJobs());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<JobOpeningDto>> getAllActiveJobs() {
        return ResponseEntity.ok(jobOpeningService.getAllActiveJobs());
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<JobOpeningDto> getJobById(@PathVariable("jobId") Long jobId) {
        return ResponseEntity.ok(jobOpeningService.getJobById(jobId));
    }

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobOpeningDto> createJob(
            @ModelAttribute CreateJobRequest createJobDto,
            @RequestParam(name = "jdFile", required = false) MultipartFile jdFile,
            @CurrentUser Long hrId) {
        createJobDto.setJdFile(jdFile);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobOpeningService.createJob(createJobDto, hrId));
    }
    // @PostMapping
    // @PreAuthorize("hasRole('HR')")
    // public ResponseEntity<JobOpeningDto> createJob(
    // @RequestParam String title,
    // @RequestParam(required = false) String summary,
    // @RequestParam(name = "jdFile", required = false) MultipartFile jdFile,
    // @RequestParam(required = false) Long jobHROwnerId,
    // @CurrentUser Long hrId) {
    // CreateJobRequest jobRequest = new CreateJobRequest();
    // jobRequest.setTitle(title);
    // jobRequest.setSummary(summary);
    // jobRequest.setJdFile(jdFile);
    // jobRequest.setJobHROwnerId(jobHROwnerId);

    // return ResponseEntity.status(HttpStatus.CREATED)
    // .body(jobOpeningService.createJob(jobRequest, hrId));
    // }

    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobOpeningDto> updateJob(
            @PathVariable("jobId") Long jobId,
            @ModelAttribute UpdateJobRequest jobRequest,
            @RequestParam(name = "jdFile", required = false) MultipartFile jdFile,
            @CurrentUser Long hrId) {
        jobRequest.setJdFile(jdFile);

        return ResponseEntity.ok(jobOpeningService.updateJob(jobId, jobRequest, hrId));
    }

    @PatchMapping("/{jobId}/deactivate")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> deactivateJob(
            @PathVariable("jobId") Long jobId,
            @CurrentUser Long hrId) {
        jobOpeningService.deactivateJob(jobId, hrId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{jobId}/activate")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> activateJob(
            @PathVariable("jobId") Long jobId,
            @CurrentUser Long hrId) {
        jobOpeningService.activateJob(jobId, hrId);
        return ResponseEntity.noContent().build();
    }
}
