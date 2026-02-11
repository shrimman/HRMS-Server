package intern.roima.hrmsbackend.dtos.Responses;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AchievementLikeDto {

    private Long likeId;
    private Long postId;
    private EmployeeSummaryDto employee;
    private LocalDateTime createdAt;
}
