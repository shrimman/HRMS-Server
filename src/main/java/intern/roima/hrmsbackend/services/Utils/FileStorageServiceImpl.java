package intern.roima.hrmsbackend.services.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import intern.roima.hrmsbackend.exceptions.FileStorageException;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path rootLocation;

    public enum FileCategory {
        PROFILE_PHOTO("profile-photos"),
        TRAVEL_DOCUMENT("travel-documents"),
        EXPENSE_PROOF("expense-proofs"),
        JOB_DESCRIPTION("job-descriptions"),
        REFERRAL_CV("referral-cvs");

        private final String directory;

        FileCategory(String directory) {
            this.directory = directory;
        }

        public String getDirectory() {
            return directory;
        }
    }

    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList("pdf", "doc", "docx", "jpg", "jpeg", "png");
    private static final List<String> CV_EXTENSIONS = Arrays.asList("pdf", "doc", "docx");

    @jakarta.annotation.PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir);
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }

            for (FileCategory category : FileCategory.values()) {
                Path categoryPath = rootLocation.resolve(category.getDirectory());
                if (!Files.exists(categoryPath)) {
                    Files.createDirectories(categoryPath);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize file storage directories", e);
        }
    }

    @Override
    public String storeProfilePhoto(MultipartFile file, Long employeeId) {
        validateFile(file, IMAGE_EXTENSIONS, "Profile photo must be an image (jpg, jpeg, png, gif)");
        String filename = generateUniqueFilename(file, "employee_" + employeeId);
        return storeFile(file, FileCategory.PROFILE_PHOTO, filename);
    }

    @Override
    public String storeTravelDocument(MultipartFile file, Long travelId, Long employeeId) {
        validateFile(file, DOCUMENT_EXTENSIONS, "Document must be a valid file type (pdf, doc, docx, jpg, jpeg, png)");
        String filename = generateUniqueFilename(file, "travel_" + travelId + "_emp_" + employeeId);
        return storeFile(file, FileCategory.TRAVEL_DOCUMENT, filename);
    }

    @Override
    public String storeExpenseProof(MultipartFile file, Long expenseId) {
        validateFile(file, DOCUMENT_EXTENSIONS, "Expense proof must be a valid file type (pdf, jpg, jpeg, png)");
        String filename = generateUniqueFilename(file, "expense_" + expenseId);
        return storeFile(file, FileCategory.EXPENSE_PROOF, filename);
    }

    @Override
    public String storeJobDescription(MultipartFile file, Long jobId) {
        validateFile(file, Arrays.asList("pdf"), "Job description must be a PDF file");
        String filename = generateUniqueFilename(file, "job_" + jobId);
        return storeFile(file, FileCategory.JOB_DESCRIPTION, filename);
    }

    @Override
    public String storeReferralCV(MultipartFile file, Long referralId) {
        validateFile(file, CV_EXTENSIONS, "CV must be a valid file type (pdf, doc, docx)");
        String filename = generateUniqueFilename(file, "referral_" + referralId);
        return storeFile(file, FileCategory.REFERRAL_CV, filename);
    }

    @Override
    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new FileStorageException("Cannot store empty file");
            }

            String filename = generateUniqueFilename(file, "");
            Path destinationFile = this.rootLocation.resolve(filename).normalize();

            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new FileStorageException("Cannot store file outside upload directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return filename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public Resource loadAsResource(String filePath) {
        try {
            Path file = rootLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("Could not read file: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new FileStorageException("Could not read file: " + filePath);
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            if (filePath == null || filePath.trim().isEmpty()) {
                return false;
            }

            Path file = rootLocation.resolve(filePath).normalize();

            if (!file.startsWith(rootLocation)) {
                throw new FileStorageException("Cannot delete file outside upload directory");
            }

            return Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file: " + filePath);
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        Path file = rootLocation.resolve(filePath).normalize();
        return Files.exists(file);
    }

    @Override
    public Path getFilePath(String filePath) {
        return rootLocation.resolve(filePath).normalize();
    }

    private String storeFile(MultipartFile file, FileCategory category, String filename) {
        try {
            if (file.isEmpty()) {
                throw new FileStorageException("Cannot store empty file");
            }

            Path categoryPath = rootLocation.resolve(category.getDirectory());
            Path destinationFile = categoryPath.resolve(filename).normalize();

            if (!destinationFile.startsWith(categoryPath)) {
                throw new FileStorageException("Cannot store file outside designated directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return category.getDirectory() + "/" + filename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file, List<String> allowedExtensions, String errorMessage) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File cannot be empty");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename).toLowerCase();

        if (!allowedExtensions.contains(extension)) {
            throw new FileStorageException(errorMessage);
        }

        if (originalFilename.contains("..")) {
            throw new FileStorageException("Filename contains invalid path sequence: " + originalFilename);
        }
    }

    private String generateUniqueFilename(MultipartFile file, String prefix) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);
        String nameWithoutExtension = originalFilename.substring(0, originalFilename.lastIndexOf('.'));

        nameWithoutExtension = nameWithoutExtension.replaceAll("[^a-zA-Z0-9_-]", "_");

        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        if (prefix != null && !prefix.isEmpty()) {
            return prefix + "_" + nameWithoutExtension + "_" + uniqueId + "." + extension;
        } else {
            return nameWithoutExtension + "_" + uniqueId + "." + extension;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
