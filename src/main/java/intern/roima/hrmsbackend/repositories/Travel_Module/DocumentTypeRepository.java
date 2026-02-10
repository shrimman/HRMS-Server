package intern.roima.hrmsbackend.repositories.Travel_Module;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Travel_Module.DocumentType;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {
    Optional<DocumentType> findByTypeName(String typeName);
}
