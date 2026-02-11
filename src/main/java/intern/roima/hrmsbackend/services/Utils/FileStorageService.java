package intern.roima.hrmsbackend.services.Utils;

import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeProfilePhoto(MultipartFile file, Long employeeId);

    String storeTravelDocument(MultipartFile file, Long travelId, Long employeeId);

    String storeExpenseProof(MultipartFile file, Long expenseId);

    String storeJobDescription(MultipartFile file, Long jobId);

    String storeReferralCV(MultipartFile file, Long referralId);

    String store(MultipartFile file);

    Resource loadAsResource(String filePath);

    boolean deleteFile(String filePath);

    boolean fileExists(String filePath);

    Path getFilePath(String filePath);

}