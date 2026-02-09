package intern.roima.hrmsbackend.dtos.Responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private boolean success;
    private String message;
    private UserDto user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long employeeId;
        private String email;
        private String firstName;
        private String lastName;
        private String roleName;
    }
}
