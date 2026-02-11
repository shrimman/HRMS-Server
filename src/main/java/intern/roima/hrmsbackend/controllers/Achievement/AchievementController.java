package intern.roima.hrmsbackend.controllers.Achievement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import intern.roima.hrmsbackend.dtos.Requests.CreateCommentRequest;
import intern.roima.hrmsbackend.dtos.Requests.CreatePostRequest;
import intern.roima.hrmsbackend.dtos.Requests.ModerationRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateCommentRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdatePostRequest;
import intern.roima.hrmsbackend.dtos.Responses.AchievementCommentDto;
import intern.roima.hrmsbackend.dtos.Responses.AchievementLikeDto;
import intern.roima.hrmsbackend.dtos.Responses.AchievementPostDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Achievement_Module.AchievementFeedService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private final AchievementFeedService achievementFeedService;

    public AchievementController(AchievementFeedService achievementFeedService) {
        this.achievementFeedService = achievementFeedService;
    }

    @GetMapping("/feed")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<AchievementPostDto>> getAllPosts(@CurrentUser Long currentEmployeeId) {
        return ResponseEntity.ok(achievementFeedService.getAllPosts(currentEmployeeId));
    }

    @GetMapping("/posts/{postId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<AchievementPostDto> getPostById(
            @PathVariable("postId") Long postId,
            @CurrentUser Long currentEmployeeId) {
        return ResponseEntity.ok(achievementFeedService.getPostById(postId, currentEmployeeId));
    }

    @GetMapping("/my-posts")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<AchievementPostDto>> getMyPosts(@CurrentUser Long employeeId) {
        return ResponseEntity.ok(achievementFeedService.getMyPosts(employeeId));
    }

    @GetMapping("/system-posts")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<AchievementPostDto>> getSystemGeneratedPosts(@CurrentUser Long currentEmployeeId) {
        return ResponseEntity.ok(achievementFeedService.getSystemGeneratedPosts(currentEmployeeId));
    }

    @PostMapping("/posts")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<AchievementPostDto> createPost(
            @Valid @RequestBody CreatePostRequest postRequest,
            @CurrentUser Long authorId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(achievementFeedService.createPost(postRequest, authorId));
    }

    @PutMapping("/posts/{postId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<AchievementPostDto> updatePost(
            @PathVariable("postId") Long postId,
            @Valid @RequestBody UpdatePostRequest postRequest,
            @CurrentUser Long employeeId) {
        return ResponseEntity.ok(achievementFeedService.updatePost(postId, postRequest, employeeId));
    }

    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> deletePost(
            @PathVariable("postId") Long postId,
            @CurrentUser Long employeeId) {
        achievementFeedService.deletePost(postId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/posts/{postId}/moderate")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> deletePostByHR(
            @PathVariable("postId") Long postId,
            @Valid @RequestBody ModerationRequest moderationRequest,
            @CurrentUser Long hrEmployeeId) {
        achievementFeedService.deletePostByHR(postId, hrEmployeeId, moderationRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/like")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<AchievementLikeDto> likePost(
            @PathVariable("postId") Long postId,
            @CurrentUser Long employeeId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(achievementFeedService.likePost(postId, employeeId));
    }

    @DeleteMapping("/posts/{postId}/like")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> unlikePost(
            @PathVariable("postId") Long postId,
            @CurrentUser Long employeeId) {
        achievementFeedService.unlikePost(postId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/{postId}/liked")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Boolean> hasUserLikedPost(
            @PathVariable("postId") Long postId,
            @CurrentUser Long employeeId) {
        return ResponseEntity.ok(achievementFeedService.hasUserLikedPost(postId, employeeId));
    }

    @GetMapping("/posts/{postId}/likes/count")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Long> getLikeCount(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(achievementFeedService.getLikeCount(postId));
    }

    @GetMapping("/posts/{postId}/likes")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<AchievementLikeDto>> getLikesForPost(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(achievementFeedService.getLikesForPost(postId));
    }

    @PostMapping("/comments")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<AchievementCommentDto> addComment(
            @Valid @RequestBody CreateCommentRequest commentRequest,
            @CurrentUser Long employeeId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(achievementFeedService.addComment(commentRequest, employeeId));
    }

    @PutMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<AchievementCommentDto> updateComment(
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody UpdateCommentRequest commentRequest,
            @CurrentUser Long employeeId) {
        return ResponseEntity.ok(achievementFeedService.updateComment(commentId, commentRequest, employeeId));
    }

    @DeleteMapping("/comments/{commentId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @CurrentUser Long employeeId) {
        achievementFeedService.deleteComment(commentId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{commentId}/moderate")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> deleteCommentByHR(
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody ModerationRequest moderationRequest,
            @CurrentUser Long hrEmployeeId) {
        achievementFeedService.deleteCommentByHR(commentId, hrEmployeeId, moderationRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/{postId}/comments/count")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Long> getCommentCount(@PathVariable Long postId) {
        return ResponseEntity.ok(achievementFeedService.getCommentCount(postId));
    }

    @GetMapping("/posts/{postId}/comments")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<AchievementCommentDto>> getCommentsForPost(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(achievementFeedService.getCommentsForPost(postId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<AchievementPostDto>> searchPosts(
            @RequestParam String keyword,
            @CurrentUser Long currentEmployeeId) {
        return ResponseEntity.ok(achievementFeedService.searchPosts(keyword, currentEmployeeId));
    }

    @GetMapping("/posts/author/{authorId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<AchievementPostDto>> filterPostsByAuthor(
            @PathVariable("authorId") Long authorId,
            @CurrentUser Long currentEmployeeId) {
        return ResponseEntity.ok(achievementFeedService.filterPostsByAuthor(authorId, currentEmployeeId));
    }

    @GetMapping("/posts/filter/date-range")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<AchievementPostDto>> filterPostsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @CurrentUser Long currentEmployeeId) {
        return ResponseEntity.ok(achievementFeedService.filterPostsByDateRange(startDate, endDate, currentEmployeeId));
    }

}
