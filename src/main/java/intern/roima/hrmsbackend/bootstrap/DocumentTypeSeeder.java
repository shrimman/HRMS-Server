package intern.roima.hrmsbackend.bootstrap;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import intern.roima.hrmsbackend.entities.Travel_Module.DocumentType;
import intern.roima.hrmsbackend.repositories.Travel_Module.DocumentTypeRepository;

@Component
public class DocumentTypeSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final DocumentTypeRepository documentTypeRepository;

    public DocumentTypeSeeder(DocumentTypeRepository documentTypeRepository) {
        this.documentTypeRepository = documentTypeRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadDocumentTypes();
    }

    private void loadDocumentTypes() {
        String[][] documentTypes = {
                { "Passport", "Travel passport documentation" },
                { "ID Card", "Government-issued identification card like Aadhaar or Driver's License" },
                { "Visa", "Travel visa documentation" },
                { "Flight Ticket", "Flight booking confirmation or ticket" },
                { "Hotel Booking", "Hotel reservation or booking confirmation" },
                { "Travel Insurance", "Travel insurance policy documents" },
                { "Vaccination Certificate", "Health and vaccination certificates" },
                { "Itinerary", "Travel itinerary and schedule" },
                { "Other", "Other travel-related documents" }
        };

        for (String[] documentType : documentTypes) {
            if (documentTypeRepository.findByTypeName(documentType[0]).isEmpty()) {
                DocumentType type = new DocumentType();
                type.setTypeName(documentType[0]);
                type.setDescription(documentType[1]);
                type.setActive(true);
                type.setCreatedAt(LocalDateTime.now());
                type.setUpdatedAt(LocalDateTime.now());
                documentTypeRepository.save(type);
            }
        }
    }
}
