package intern.roima.hrmsbackend.services.Travel_Module;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import intern.roima.hrmsbackend.dtos.Requests.UploadTravelDocumentRequest;
import intern.roima.hrmsbackend.dtos.Responses.DocumentTypeDto;
import intern.roima.hrmsbackend.dtos.Responses.TravelDocumentDto;

public interface TravelDocumentService {

    TravelDocumentDto uploadDocument(UploadTravelDocumentRequest request, MultipartFile file, Long uploadedById);

    TravelDocumentDto getDocumentById(Long documentId);

    List<TravelDocumentDto> getDocumentsForTravel(Long travelPlanId);

    List<TravelDocumentDto> getDocumentsForTravelAndEmployee(Long travelPlanId, Long employeeId);

    List<TravelDocumentDto> getDocumentsForEmployee(Long employeeId);

    List<TravelDocumentDto> getDocumentsForManager(Long managerId);

    List<TravelDocumentDto> getDocumentsByType(Long documentTypeId);

    List<TravelDocumentDto> getDocumentsUploadedByHR(Long travelPlanId);

    List<TravelDocumentDto> getDocumentsUploadedByEmployee(Long travelPlanId, Long employeeId);

    void deleteDocument(Long documentId, Long employeeId);

    List<DocumentTypeDto> getAllDocumentTypes();

}