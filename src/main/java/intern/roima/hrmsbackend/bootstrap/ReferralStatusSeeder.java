package intern.roima.hrmsbackend.bootstrap;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import intern.roima.hrmsbackend.entities.Job_Module.ReferralStatus;
import intern.roima.hrmsbackend.repositories.Job_Module.ReferralStatusRepository;

@Component
public class ReferralStatusSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final ReferralStatusRepository referralStatusRepository;

    public ReferralStatusSeeder(ReferralStatusRepository referralStatusRepository) {
        this.referralStatusRepository = referralStatusRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadReferralStatuses();
    }

    private void loadReferralStatuses() {
        String[] statusNames = {
                "SUBMITTED",
                "UNDER_REVIEW",
                "SHORTLISTED",
                "INTERVIEW_SCHEDULED",
                "SELECTED",
                "REJECTED",
                "WITHDRAWN"
        };

        for (String statusName : statusNames) {
            if (referralStatusRepository.findByStatusName(statusName).isEmpty()) {
                ReferralStatus status = new ReferralStatus();
                status.setStatusName(statusName);
                status.setUpdatedAt(LocalDateTime.now());
                referralStatusRepository.save(status);
            }
        }
    }
}
