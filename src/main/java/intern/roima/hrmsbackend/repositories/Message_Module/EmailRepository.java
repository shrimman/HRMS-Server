package intern.roima.hrmsbackend.repositories.Message_Module;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Message_Module.EmailTemplate;

public interface EmailRepository extends JpaRepository<EmailTemplate, Long> {

}
