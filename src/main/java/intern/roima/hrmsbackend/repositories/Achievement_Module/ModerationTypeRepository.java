package intern.roima.hrmsbackend.repositories.Achievement_Module;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Achievement_Module.ModerationTypes;

public interface ModerationTypeRepository extends JpaRepository<ModerationTypes, Long> {
    Optional<ModerationTypes> findByTypeName(String typeName);
}
