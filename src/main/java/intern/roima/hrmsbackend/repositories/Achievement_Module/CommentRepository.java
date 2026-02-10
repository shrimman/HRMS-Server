package intern.roima.hrmsbackend.repositories.Achievement_Module;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Achievement_Module.AchievementComments;

public interface CommentRepository extends JpaRepository<AchievementComments, Long> {

}
