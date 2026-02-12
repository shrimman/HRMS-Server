package intern.roima.hrmsbackend.controllers.Job;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import intern.roima.hrmsbackend.dtos.Requests.CreateReferralRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateReferralStatusRequest;
import intern.roima.hrmsbackend.dtos.Responses.JobReferralDto;
import intern.roima.hrmsbackend.dtos.Responses.ReferralStatusDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Job_Module.JobReferralService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/referrals")
public class JobReferralController {

    private final JobReferralService jobReferralService;

    public JobReferralController(JobReferralService jobReferralService) {
        this.jobReferralService = jobReferralService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<JobReferralDto> createReferral(
            @Valid @ModelAttribute CreateReferralRequest referralRequest,
            @RequestParam(name = "cvFile", required = false) MultipartFile cvFile,
            @CurrentUser Long referrerId) {
        referralRequest.setCvFile(cvFile);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobReferralService.createReferral(referralRequest, referrerId));
    }

    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<JobReferralDto>> getAllReferrals() {
        return ResponseEntity.ok(jobReferralService.getAllReferrals());
    }

    @GetMapping("/{referralId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<JobReferralDto> getReferralById(@PathVariable("referralId") Long referralId) {
        return ResponseEntity.ok(jobReferralService.getReferralById(referralId));
    }

    @GetMapping("/my-referrals")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<JobReferralDto>> getMyReferrals(@CurrentUser Long employeeId) {
        return ResponseEntity.ok(jobReferralService.getMyReferrals(employeeId));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<JobReferralDto>> getReferralsByJob(@PathVariable("jobId") Long jobId) {
        return ResponseEntity.ok(jobReferralService.getReferralsByJob(jobId));
    }

    @GetMapping("/status/{statusId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<JobReferralDto>> getReferralsByStatus(@PathVariable("statusId") Long statusId) {
        return ResponseEntity.ok(jobReferralService.getReferralsByStatus(statusId));
    }

    @PutMapping("/{referralId}/status")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobReferralDto> updateReferralStatus(
            @PathVariable("referralId") Long referralId,
            @RequestParam("statusId") Long statusId,
            @CurrentUser Long hrId) {
        UpdateReferralStatusRequest statusRequest = new UpdateReferralStatusRequest();
        statusRequest.setStatusId(statusId);

        return ResponseEntity.ok(jobReferralService.updateReferralStatus(referralId, statusRequest, hrId));
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<ReferralStatusDto>> getAllReferralStatuses() {
        return ResponseEntity.ok(jobReferralService.getAllReferralStatuses());
    }

    @DeleteMapping("/{referralId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> deleteReferral(@PathVariable("referralId") Long referralId) {
        jobReferralService.deleteReferral(referralId);
        return ResponseEntity.noContent().build();
    }
}
