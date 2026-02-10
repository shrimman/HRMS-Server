package intern.roima.hrmsbackend.bootstrap;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import intern.roima.hrmsbackend.entities.Travel_Module.ExpenseStatus;
import intern.roima.hrmsbackend.repositories.Travel_Module.ExpenseStatusRepository;

@Component
public class ExpenseStatusSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final ExpenseStatusRepository expenseStatusRepository;

    public ExpenseStatusSeeder(ExpenseStatusRepository expenseStatusRepository) {
        this.expenseStatusRepository = expenseStatusRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadExpenseStatuses();
    }

    private void loadExpenseStatuses() {
        String[] statusNames = {
                "PENDING",
                "APPROVED",
                "REJECTED"
        };

        for (String statusName : statusNames) {
            if (expenseStatusRepository.findByStatusName(statusName).isEmpty()) {
                ExpenseStatus status = new ExpenseStatus();
                status.setStatusName(statusName);
                status.setCreatedAt(LocalDateTime.now());
                status.setUpdatedAt(LocalDateTime.now());
                expenseStatusRepository.save(status);
            }
        }
    }
}
