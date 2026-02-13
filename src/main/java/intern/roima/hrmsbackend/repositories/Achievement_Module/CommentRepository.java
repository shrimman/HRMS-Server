package intern.roima.hrmsbackend.repositories.Achievement_Module;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Achievement_Module.AchievementComments;

public interface CommentRepository extends JpaRepository<AchievementComments, Long> {

    List<AchievementComments> findByPost_PostIdAndIsDeletedFalseOrderByCreatedAtAsc(Long postId);

    Long countByPost_PostIdAndIsDeletedFalse(Long postId);

    List<AchievementComments> findByAuthor_EmployeeIdAndIsDeletedFalse(Long employeeId);
}
