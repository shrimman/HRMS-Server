package intern.roima.hrmsbackend.dtos.Requests;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJobRequest {
    private String title;
    private String summary;
    private MultipartFile jdFile;
    private Long jobHROwnerId;
}
