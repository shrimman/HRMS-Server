package intern.roima.hrmsbackend.repositories.Achievement_Module;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Achievement_Module.AchievementModerations;

public interface ModerationRepository extends JpaRepository<AchievementModerations, Long> {

    List<AchievementModerations> findByPost_PostId(Long postId);

    List<AchievementModerations> findByComment_CommentId(Long commentId);

    List<AchievementModerations> findByDeletedBy_EmployeeId(Long employeeId);

    List<AchievementModerations> findByModerationType_ModerationTypeId(Long moderationTypeId);
}
