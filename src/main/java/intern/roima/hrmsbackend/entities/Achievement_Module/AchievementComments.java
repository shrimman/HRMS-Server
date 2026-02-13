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
@Table(name = "AchievementComments")
public class AchievementComments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PostId", referencedColumnName = "postId", nullable = false)
    private AchievementPosts post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AuthorId", referencedColumnName = "employeeId", nullable = false)
    private Employees author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UpdatedById", referencedColumnName = "employeeId")
    private Employees updatedByEmployee;

    @Column(nullable = false)
    private Boolean isDeleted = false;

}
