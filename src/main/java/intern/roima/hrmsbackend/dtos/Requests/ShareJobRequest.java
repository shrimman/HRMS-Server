package intern.roima.hrmsbackend.dtos.Requests;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShareJobRequest {
    @NotEmpty(message = "Recipient email list cannot be empty")
    private List<String> recipientEmails;
}
