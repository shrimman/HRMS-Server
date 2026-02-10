package intern.roima.hrmsbackend.bootstrap;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import intern.roima.hrmsbackend.entities.Travel_Module.ExpenseType;
import intern.roima.hrmsbackend.repositories.Travel_Module.ExpenseTypeRepository;

@Component
public class ExpenseTypeSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final ExpenseTypeRepository expenseTypeRepository;

    public ExpenseTypeSeeder(ExpenseTypeRepository expenseTypeRepository) {
        this.expenseTypeRepository = expenseTypeRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadExpenseTypes();
    }

    private void loadExpenseTypes() {
        String[][] expenseTypes = {
                { "Transportation",
                        "Expenses for transportation including flights, trains, buses, taxis, and car rentals" },
                { "Accommodation", "Hotel, hostel, or other lodging expenses" },
                { "Meals", "Food and beverage expenses during travel" },
                { "Conference/Event Fees", "Registration fees for conferences, seminars, or events" },
                { "Communication", "Phone, internet, and other communication expenses" },
                { "Miscellaneous", "Other travel-related expenses not covered by specific categories" }
        };

        for (String[] expenseType : expenseTypes) {
            if (expenseTypeRepository.findByTypeName(expenseType[0]).isEmpty()) {
                ExpenseType type = new ExpenseType();
                type.setTypeName(expenseType[0]);
                type.setDescription(expenseType[1]);
                type.setActive(true);
                type.setCreatedAt(LocalDateTime.now());
                type.setUpdatedAt(LocalDateTime.now());
                expenseTypeRepository.save(type);
            }
        }
    }
}
