package intern.roima.hrmsbackend.services.Job_Module;

import java.util.List;

import intern.roima.hrmsbackend.dtos.Requests.CreateReferralRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateReferralStatusRequest;
import intern.roima.hrmsbackend.dtos.Responses.JobReferralDto;
import intern.roima.hrmsbackend.dtos.Responses.ReferralStatusDto;

public interface JobReferralService {

    JobReferralDto createReferral(CreateReferralRequest referralRequest, Long referrerId);

    JobReferralDto getReferralById(Long referralId);

    List<JobReferralDto> getMyReferrals(Long employeeId);

    List<JobReferralDto> getAllReferrals();

    List<JobReferralDto> getReferralsByJob(Long jobId);

    List<JobReferralDto> getReferralsByStatus(Long statusId);

    JobReferralDto updateReferralStatus(Long referralId, UpdateReferralStatusRequest statusRequest, Long hrId);

    List<ReferralStatusDto> getAllReferralStatuses();

    void deleteReferral(Long referralId);
}
