package intern.roima.hrmsbackend.services.Travel_Module.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import intern.roima.hrmsbackend.dtos.Requests.UploadTravelDocumentRequest;
import intern.roima.hrmsbackend.dtos.Responses.DocumentTypeDto;
import intern.roima.hrmsbackend.dtos.Responses.TravelDocumentDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.entities.Travel_Module.DocumentType;
import intern.roima.hrmsbackend.entities.Travel_Module.TravelDocuments;
import intern.roima.hrmsbackend.entities.Travel_Module.TravelPlans;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.repositories.Travel_Module.DocumentTypeRepository;
import intern.roima.hrmsbackend.repositories.Travel_Module.TravelDocumentRepository;
import intern.roima.hrmsbackend.repositories.Travel_Module.TravelPlanRepository;
import intern.roima.hrmsbackend.services.Travel_Module.TravelDocumentService;
import intern.roima.hrmsbackend.services.Utils.FileStorageService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class TravelDocumentServiceImpl implements TravelDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(TravelDocumentServiceImpl.class);

    private final TravelDocumentRepository travelDocumentRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final FileStorageService fileStorageService;

    public TravelDocumentServiceImpl(TravelDocumentRepository travelDocumentRepository,
            TravelPlanRepository travelPlanRepository,
            EmployeeRepository employeeRepository,
            DocumentTypeRepository documentTypeRepository,
            FileStorageService fileStorageService) {
        this.travelDocumentRepository = travelDocumentRepository;
        this.travelPlanRepository = travelPlanRepository;
        this.employeeRepository = employeeRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public TravelDocumentDto uploadDocument(UploadTravelDocumentRequest request, MultipartFile file,
            Long uploadedById) {
        logger.info("Uploading document for travel plan ID: {} by user ID: {}",
                request.getTravelPlanId(), uploadedById);

        try {
            TravelPlans travelPlan = travelPlanRepository.findById(request.getTravelPlanId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Travel plan not found with ID: " + request.getTravelPlanId()));

            Employees employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Employee not found with ID: " + request.getEmployeeId()));

            Employees uploadedBy = employeeRepository.findById(uploadedById)
                    .orElseThrow(
                            () -> new EntityNotFoundException("Uploader employee not found with ID: " + uploadedById));

            DocumentType documentType = documentTypeRepository.findById(request.getDocumentTypeId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Document type not found with ID: " + request.getDocumentTypeId()));

            if (file.isEmpty()) {
                throw new IllegalArgumentException("Cannot upload empty file");
            }

            String filePath = fileStorageService.storeTravelDocument(file, request.getTravelPlanId(),
                    request.getEmployeeId());

            TravelDocuments document = new TravelDocuments();
            document.setTravelPlan(travelPlan);
            document.setEmployee(employee);
            document.setDocumentType(documentType);
            document.setDocumentName(request.getDocumentName());
            document.setDocumentPath(filePath);
            document.setUploadedBy(uploadedBy);
            document.setUploadedAt(LocalDateTime.now());

            TravelDocuments savedDocument = travelDocumentRepository.save(document);

            logger.info("Successfully uploaded document ID: {} for travel plan ID: {}",
                    savedDocument.getDocumentId(), request.getTravelPlanId());

            return toTravelDocumentDto(savedDocument);

        } catch (EntityNotFoundException | IllegalArgumentException e) {
            logger.error("Error uploading travel document: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error uploading travel document: {}", e.getMessage());
            throw new RuntimeException("Database error while uploading travel document", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TravelDocumentDto getDocumentById(Long documentId) {
        logger.info("Fetching document by ID: {}", documentId);

        try {
            TravelDocuments document = travelDocumentRepository.findById(documentId)
                    .orElseThrow(() -> new EntityNotFoundException("Document not found with ID: " + documentId));

            return toTravelDocumentDto(document);

        } catch (EntityNotFoundException e) {
            logger.error("Error fetching document: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error fetching document: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching document", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelDocumentDto> getDocumentsForTravel(Long travelPlanId) {
        logger.info("Fetching documents for travel plan ID: {}", travelPlanId);

        try {
            List<TravelDocumentDto> documents = travelDocumentRepository.findByTravelPlan_TravelId(travelPlanId)
                    .stream()
                    .map(this::toTravelDocumentDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} documents for travel plan ID: {}", documents.size(), travelPlanId);
            return documents;

        } catch (DataAccessException e) {
            logger.error("Database error fetching documents for travel: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching documents for travel", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelDocumentDto> getDocumentsForTravelAndEmployee(Long travelPlanId, Long employeeId) {
        logger.info("Fetching documents for travel plan ID: {} and employee ID: {}", travelPlanId, employeeId);

        try {
            List<TravelDocumentDto> documents = travelDocumentRepository
                    .findByTravelPlan_TravelIdAndEmployee_EmployeeId(travelPlanId, employeeId)
                    .stream()
                    .map(this::toTravelDocumentDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} documents for travel plan ID: {} and employee ID: {}",
                    documents.size(), travelPlanId, employeeId);
            return documents;

        } catch (DataAccessException e) {
            logger.error("Database error fetching documents for travel and employee: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching documents for travel and employee", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelDocumentDto> getDocumentsForEmployee(Long employeeId) {
        logger.info("Fetching documents for employee ID: {}", employeeId);

        try {
            List<TravelDocumentDto> documents = travelDocumentRepository.findByEmployee_EmployeeId(employeeId)
                    .stream()
                    .map(this::toTravelDocumentDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} documents for employee ID: {}", documents.size(), employeeId);
            return documents;

        } catch (DataAccessException e) {
            logger.error("Database error fetching documents for employee: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching documents for employee", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelDocumentDto> getDocumentsForManager(Long managerId) {
        logger.info("Fetching documents for manager ID: {}", managerId);

        try {
            List<Employees> teamMembers = employeeRepository.findByManagerEmployeeId(managerId);

            List<TravelDocumentDto> documents = teamMembers.stream()
                    .flatMap(employee -> travelDocumentRepository.findByEmployee_EmployeeId(employee.getEmployeeId())
                            .stream())
                    .map(this::toTravelDocumentDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} documents for manager ID: {} team members", documents.size(), managerId);
            return documents;

        } catch (EntityNotFoundException e) {
            logger.error("Error fetching documents for manager: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error fetching documents for manager: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching documents for manager", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelDocumentDto> getDocumentsByType(Long documentTypeId) {
        logger.info("Fetching documents by type ID: {}", documentTypeId);

        try {
            List<TravelDocumentDto> documents = travelDocumentRepository
                    .findByDocumentType_DocumentTypeId(documentTypeId)
                    .stream()
                    .map(this::toTravelDocumentDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} documents of type ID: {}", documents.size(), documentTypeId);
            return documents;

        } catch (DataAccessException e) {
            logger.error("Database error fetching documents by type: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching documents by type", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelDocumentDto> getDocumentsUploadedByHR(Long travelPlanId) {
        logger.info("Fetching HR uploaded documents for travel plan ID: {}", travelPlanId);

        try {
            List<TravelDocumentDto> documents = travelDocumentRepository.findByTravelPlan_TravelId(travelPlanId)
                    .stream()
                    .filter(doc -> "HR".equals(doc.getUploadedBy().getRole().getRoleName()))
                    .map(this::toTravelDocumentDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} HR uploaded documents for travel plan ID: {}", documents.size(), travelPlanId);
            return documents;

        } catch (DataAccessException e) {
            logger.error("Database error fetching HR uploaded documents: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching HR uploaded documents", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TravelDocumentDto> getDocumentsUploadedByEmployee(Long travelPlanId, Long employeeId) {
        logger.info("Fetching employee uploaded documents for travel plan ID: {} and employee ID: {}",
                travelPlanId, employeeId);

        try {
            List<TravelDocumentDto> documents = travelDocumentRepository
                    .findByTravelPlan_TravelIdAndEmployee_EmployeeId(travelPlanId, employeeId)
                    .stream()
                    .filter(doc -> employeeId.equals(doc.getUploadedBy().getEmployeeId()))
                    .map(this::toTravelDocumentDto)
                    .collect(Collectors.toList());

            logger.debug("Found {} employee uploaded documents for travel plan ID: {} and employee ID: {}",
                    documents.size(), travelPlanId, employeeId);
            return documents;

        } catch (DataAccessException e) {
            logger.error("Database error fetching employee uploaded documents: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching employee uploaded documents", e);
        }
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId, Long employeeId) {
        logger.info("Deleting document ID: {} by employee ID: {}", documentId, employeeId);

        try {
            TravelDocuments document = travelDocumentRepository.findById(documentId)
                    .orElseThrow(() -> new EntityNotFoundException("Document not found with ID: " + documentId));

            if (fileStorageService.fileExists(document.getDocumentPath())) {
                fileStorageService.deleteFile(document.getDocumentPath());
            }

            travelDocumentRepository.delete(document);

            logger.info("Successfully deleted document ID: {}", documentId);

        } catch (EntityNotFoundException e) {
            logger.error("Error deleting document: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error deleting document: {}", e.getMessage());
            throw new RuntimeException("Database error while deleting document", e);
        }
    }

    @Override
    public List<DocumentTypeDto> getAllDocumentTypes() {
        return documentTypeRepository.findAll().stream()
                .map(dt -> new DocumentTypeDto(dt.getDocumentTypeId(), dt.getTypeName()))
                .collect(Collectors.toList());
    }

    private TravelDocumentDto toTravelDocumentDto(TravelDocuments document) {
        TravelDocumentDto dto = new TravelDocumentDto();
        dto.setDocumentId(document.getDocumentId());
        dto.setTravelPlanId(document.getTravelPlan().getTravelId());
        dto.setTravelPlanTitle(document.getTravelPlan().getTitle());
        dto.setEmployeeId(document.getEmployee().getEmployeeId());
        dto.setEmployeeName(document.getEmployee().getFirstName() + " " + document.getEmployee().getLastName());
        dto.setDocumentTypeId(document.getDocumentType().getDocumentTypeId());
        dto.setDocumentTypeName(document.getDocumentType().getTypeName());
        dto.setDocumentName(document.getDocumentName());
        dto.setDocumentPath(document.getDocumentPath());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setUploadedById(document.getUploadedBy().getEmployeeId());
        dto.setUploadedByName(document.getUploadedBy().getFirstName() + " " +
                document.getUploadedBy().getLastName());

        return dto;
    }
}
