package intern.roima.hrmsbackend.bootstrap;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.entities.Employee_Module.Roles;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.repositories.Employee_Module.RoleRepository;

@Component
public class SuperHRSeeding implements ApplicationListener<ContextRefreshedEvent> {
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public SuperHRSeeding(EmployeeRepository employeeRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadSuperHR();
    }

    @Value("${SUPERHR_EMAIL}")
    String superHREmail;
    @Value("${SUPERHR_PASSWORD}")
    String superHRPassword;

    private void loadSuperHR() {
        if (employeeRepository.findByEmail(superHREmail).isEmpty()) {
            Employees superHR = new Employees();
            superHR.setFirstName("Super");
            superHR.setLastName("HR");
            superHR.setEmail(superHREmail);
            superHR.setPasswordHash(passwordEncoder.encode(superHRPassword));
            superHR.setDateOfBirth(LocalDate.of(1990, 1, 1));
            superHR.setDateOfJoining(LocalDate.now());
            superHR.setActive(true);
            Roles role = roleRepository.findById(2) // 2 represents HR role in the Roles table
                    .orElseThrow(() -> new RuntimeException("HR Role not found in database"));
            superHR.setRole(role);
            System.out.println("Creating Super HR with email: " + superHREmail);
            employeeRepository.save(superHR);
        }
    }
}
