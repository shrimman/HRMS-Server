package intern.roima.hrmsbackend.bootstrap;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import intern.roima.hrmsbackend.entities.Employee_Module.Departments;
import intern.roima.hrmsbackend.repositories.Employee_Module.DepartmentRepository;

@Component
public class DepartmentSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final DepartmentRepository departmentRepository;

    public DepartmentSeeder(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadDepartments();
    }

    private void loadDepartments() {
        String[] departments = {
                "IT", "Human Resources", "Finance", "Testing", "Development",
                "Customer Support", "Business Analysis"
        };

        for (String department : departments) {
            if (departmentRepository.findByDepartmentName(department).isEmpty()) {
                Departments newDepartment = new Departments();
                newDepartment.setDepartmentName(department);
                departmentRepository.save(newDepartment);
            }
        }
    }
}
