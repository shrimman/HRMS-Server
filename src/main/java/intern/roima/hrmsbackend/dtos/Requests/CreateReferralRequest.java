package intern.roima.hrmsbackend.dtos.Requests;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReferralRequest {
    private Long jobId;

    @NotBlank
    private String friendName;

    @Email
    @NotBlank
    private String friendEmail;

    private MultipartFile cvFile;

    private String note;
}
