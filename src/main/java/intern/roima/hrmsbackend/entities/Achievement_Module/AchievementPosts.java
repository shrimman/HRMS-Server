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
@Table(name = "AchievementPost")
public class AchievementPosts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long PostId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AuthorId", referencedColumnName = "EmployeeId", nullable = false)
    private Employees author;

    @Column(nullable = false, length = 255)
    private String Title;

    @Column(columnDefinition = "TEXT")
    private String Description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime CreatedAt;

    @Column(nullable = false)
    private Boolean IsSystemGenerated;

}
