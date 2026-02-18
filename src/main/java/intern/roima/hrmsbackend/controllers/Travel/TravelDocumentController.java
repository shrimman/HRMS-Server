package intern.roima.hrmsbackend.controllers.Travel;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import intern.roima.hrmsbackend.dtos.Requests.UploadTravelDocumentRequest;
import intern.roima.hrmsbackend.dtos.Responses.DocumentTypeDto;
import intern.roima.hrmsbackend.dtos.Responses.TravelDocumentDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Travel_Module.TravelDocumentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/travel-documents")
public class TravelDocumentController {

    private final TravelDocumentService travelDocumentService;

    public TravelDocumentController(TravelDocumentService travelDocumentService) {
        this.travelDocumentService = travelDocumentService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('HR', 'EMPLOYEE')")
    public ResponseEntity<TravelDocumentDto> uploadDocument(
            @Valid @RequestParam("travelPlanId") Long travelPlanId,
            @RequestParam("employeeId") Long employeeId,
            @RequestParam("documentTypeId") Long documentTypeId,
            @RequestParam("documentName") String documentName,
            @RequestParam("file") MultipartFile file,
            @CurrentUser Long uploadedById) {

        UploadTravelDocumentRequest request = new UploadTravelDocumentRequest();
        request.setTravelPlanId(travelPlanId);
        request.setEmployeeId(employeeId);
        request.setDocumentTypeId(documentTypeId);
        request.setDocumentName(documentName);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(travelDocumentService.uploadDocument(request, file, uploadedById));
    }

    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<DocumentTypeDto>> getAllDocumentTypes() {
        return ResponseEntity.ok(travelDocumentService.getAllDocumentTypes());
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<TravelDocumentDto> getDocumentById(
            @PathVariable("documentId") Long documentId) {
        return ResponseEntity.ok(travelDocumentService.getDocumentById(documentId));
    }

    @GetMapping("/travel/{travelId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<TravelDocumentDto>> getDocumentsForTravel(
            @PathVariable("travelId") Long travelId) {
        return ResponseEntity.ok(travelDocumentService.getDocumentsForTravel(travelId));
    }

    @GetMapping("/travel/{travelId}/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<TravelDocumentDto>> getDocumentsForTravelAndEmployee(
            @PathVariable("travelId") Long travelId,
            @PathVariable("employeeId") Long employeeId) {
        return ResponseEntity.ok(travelDocumentService.getDocumentsForTravelAndEmployee(travelId, employeeId));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<TravelDocumentDto>> getDocumentsForEmployee(
            @PathVariable("employeeId") Long employeeId) {
        return ResponseEntity.ok(travelDocumentService.getDocumentsForEmployee(employeeId));
    }

    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<TravelDocumentDto>> getDocumentsForManager(
            @PathVariable("managerId") Long managerId) {
        return ResponseEntity.ok(travelDocumentService.getDocumentsForManager(managerId));
    }

    @GetMapping("/type/{documentTypeId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER')")
    public ResponseEntity<List<TravelDocumentDto>> getDocumentsByType(
            @PathVariable("documentTypeId") Long documentTypeId) {
        return ResponseEntity.ok(travelDocumentService.getDocumentsByType(documentTypeId));
    }

    @GetMapping("/travel/{travelId}/by-hr")
    @PreAuthorize("hasAnyRole('HR','MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<TravelDocumentDto>> getDocumentsUploadedByHR(
            @PathVariable("travelId") Long travelId) {
        return ResponseEntity.ok(travelDocumentService.getDocumentsUploadedByHR(travelId));
    }

    @GetMapping("/travel/{travelId}/by-employee/{employeeId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<TravelDocumentDto>> getDocumentsUploadedByEmployee(
            @PathVariable("travelId") Long travelId,
            @PathVariable("employeeId") Long employeeId) {
        return ResponseEntity.ok(travelDocumentService.getDocumentsUploadedByEmployee(travelId, employeeId));
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasAnyRole('HR', 'EMPLOYEE')")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable("documentId") Long documentId,
            @CurrentUser Long employeeId) {
        travelDocumentService.deleteDocument(documentId, employeeId);
        return ResponseEntity.noContent().build();
    }
}
