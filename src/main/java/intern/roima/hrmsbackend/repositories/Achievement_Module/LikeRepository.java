package intern.roima.hrmsbackend.repositories.Achievement_Module;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Achievement_Module.AchievementLikes;

public interface LikeRepository extends JpaRepository<AchievementLikes, Long> {

    Optional<AchievementLikes> findByPost_PostIdAndEmployee_EmployeeId(Long postId, Long employeeId);

    boolean existsByPost_PostIdAndEmployee_EmployeeId(Long postId, Long employeeId);

    void deleteByPost_PostIdAndEmployee_EmployeeId(Long postId, Long employeeId);

    Long countByPost_PostId(Long postId);

    List<AchievementLikes> findByPost_PostId(Long postId);
}
