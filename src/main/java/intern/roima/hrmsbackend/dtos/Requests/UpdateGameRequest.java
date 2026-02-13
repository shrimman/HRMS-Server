package intern.roima.hrmsbackend.dtos.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGameRequest {

    @NotBlank(message = "Game name is required")
    @Size(max = 255, message = "Game name cannot exceed 255 characters")
    private String gameName;
}
