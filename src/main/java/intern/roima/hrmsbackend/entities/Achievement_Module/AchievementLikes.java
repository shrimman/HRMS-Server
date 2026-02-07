package intern.roima.hrmsbackend.entities.Achievement_Module;

import java.io.Serializable;

import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@Table(name = "PostLike")
@IdClass(PostLikeId.class)
public class AchievementLikes {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PostId", referencedColumnName = "postId", nullable = false)
    private AchievementPosts post;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmployeeId", referencedColumnName = "employeeId", nullable = false)
    private Employees employee;

}

class PostLikeId implements Serializable {
    private Long post;
    private Long employee;
}
