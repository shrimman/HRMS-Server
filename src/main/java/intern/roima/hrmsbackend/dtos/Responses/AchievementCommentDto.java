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
public class AchievementCommentDto {
    private Long commentId;
    private Long postId;
    private EmployeeSummaryDto author;
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
