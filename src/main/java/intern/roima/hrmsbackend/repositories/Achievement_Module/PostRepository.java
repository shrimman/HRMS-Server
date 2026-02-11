package intern.roima.hrmsbackend.repositories.Achievement_Module;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import intern.roima.hrmsbackend.entities.Achievement_Module.AchievementPosts;

public interface PostRepository extends JpaRepository<AchievementPosts, Long> {

    List<AchievementPosts> findByAuthor_EmployeeId(Long employeeId);

    List<AchievementPosts> findByIsSystemGenerated(Boolean isSystemGenerated);

    @Query("SELECT p FROM AchievementPosts p WHERE p.title LIKE %:keyword% OR p.description LIKE %:keyword% ORDER BY p.createdAt DESC")
    List<AchievementPosts> searchByKeyword(@Param("keyword") String keyword);

    List<AchievementPosts> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<AchievementPosts> findAllByOrderByCreatedAtDesc();
}
