package intern.roima.hrmsbackend.entities.Achievement_Module;

import java.time.LocalDateTime;

import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AchievementModerations")
public class AchievementModerations {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long moderationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PostId", referencedColumnName = "postId")
    private AchievementPosts post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CommentId", referencedColumnName = "commentId")
    private AchievementComments comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ModerationTypeId", referencedColumnName = "moderationTypeId", nullable = false)
    private ModerationTypes moderationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DeletedById", referencedColumnName = "employeeId", nullable = false)
    private Employees deletedBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private LocalDateTime deletedAt;

}
