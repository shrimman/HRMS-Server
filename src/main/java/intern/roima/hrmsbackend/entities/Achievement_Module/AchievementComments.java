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
@Table(name = "PostComment")
public class AchievementComments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long CommentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PostId", referencedColumnName = "PostId", nullable = false)
    private AchievementPosts post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AuthorId", referencedColumnName = "EmployeeId", nullable = false)
    private Employees author;

    @Column(name = "Text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false, updatable = false)
    private LocalDateTime CreatedAt;

}
