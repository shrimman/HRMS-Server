package intern.roima.hrmsbackend.bootstrap;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import intern.roima.hrmsbackend.entities.Employee_Module.Designations;
import intern.roima.hrmsbackend.repositories.Employee_Module.DesignationRepository;

@Component
public class DesignationSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final DesignationRepository designationRepository;

    public DesignationSeeder(DesignationRepository designationRepository) {
        this.designationRepository = designationRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadDesignations();
    }

    private void loadDesignations() {
        String[] designations = {
                "CEO", "CTO", "CFO", "Software Engineer", "Senior Software Engineer",
                "Lead Software Engineer",
                "Project Manager", "HR Manager", "Finance Manager", "Marketing Manager",
                "Sales Manager", "Customer Support Representative", "Business Analyst"
        };

        for (String designation : designations) {
            if (designationRepository.findByDesignationName(designation).isEmpty()) {
                Designations newDesignation = new Designations();
                newDesignation.setDesignationName(designation);
                designationRepository.save(newDesignation);
            }
        }
    }
}
