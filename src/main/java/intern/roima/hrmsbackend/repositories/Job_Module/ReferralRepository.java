package intern.roima.hrmsbackend.repositories.Job_Module;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Job_Module.Referrals;

public interface ReferralRepository extends JpaRepository<Referrals, Long> {
    
}
