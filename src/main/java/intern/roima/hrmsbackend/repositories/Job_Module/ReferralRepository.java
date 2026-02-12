package intern.roima.hrmsbackend.repositories.Job_Module;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Job_Module.Referrals;

public interface ReferralRepository extends JpaRepository<Referrals, Long> {
    List<Referrals> findByReferrer_EmployeeId(Long employeeId);
    
    List<Referrals> findByJobOpening_JobId(Long jobId);
    
    List<Referrals> findByReferralStatus_StatusId(Long statusId);
}
