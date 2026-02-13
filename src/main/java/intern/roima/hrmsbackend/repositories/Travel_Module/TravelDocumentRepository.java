package intern.roima.hrmsbackend.repositories.Travel_Module;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Travel_Module.TravelDocuments;

public interface TravelDocumentRepository extends JpaRepository<TravelDocuments, Long> {

    List<TravelDocuments> findByTravelPlan_TravelId(Long travelPlanId);

    List<TravelDocuments> findByTravelPlan_TravelIdAndEmployee_EmployeeId(Long travelPlanId, Long employeeId);

    List<TravelDocuments> findByEmployee_EmployeeId(Long employeeId);

    List<TravelDocuments> findByDocumentType_DocumentTypeId(Long documentTypeId);

    List<TravelDocuments> findByUploadedBy_EmployeeId(Long uploadedById);

}
