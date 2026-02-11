package intern.roima.hrmsbackend.services.Achievement_Module;

import java.time.LocalDateTime;
import java.util.List;

import intern.roima.hrmsbackend.dtos.Requests.CreateCommentRequest;
import intern.roima.hrmsbackend.dtos.Requests.CreatePostRequest;
import intern.roima.hrmsbackend.dtos.Requests.ModerationRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateCommentRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdatePostRequest;
import intern.roima.hrmsbackend.dtos.Responses.AchievementCommentDto;
import intern.roima.hrmsbackend.dtos.Responses.AchievementLikeDto;
import intern.roima.hrmsbackend.dtos.Responses.AchievementPostDto;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;

public interface AchievementFeedService {

    List<AchievementPostDto> getAllPosts(@CurrentUser Long currentEmployeeId);

    AchievementPostDto getPostById(Long postId, @CurrentUser Long currentEmployeeId);

    List<AchievementPostDto> getMyPosts(@CurrentUser Long employeeId);

    List<AchievementPostDto> getSystemGeneratedPosts(@CurrentUser Long currentEmployeeId);

    AchievementPostDto createPost(CreatePostRequest postRequest, @CurrentUser Long authorId);

    AchievementPostDto updatePost(Long postId, UpdatePostRequest postRequest, @CurrentUser Long employeeId);

    void deletePost(Long postId, @CurrentUser Long employeeId);

    void deletePostByHR(Long postId, @CurrentUser Long hrEmployeeId, ModerationRequest moderationRequest);

    AchievementLikeDto likePost(Long postId, @CurrentUser Long employeeId);

    void unlikePost(Long postId, @CurrentUser Long employeeId);

    boolean hasUserLikedPost(Long postId, @CurrentUser Long employeeId);

    Long getLikeCount(Long postId);

    List<AchievementLikeDto> getLikesForPost(Long postId);

    AchievementCommentDto addComment(CreateCommentRequest commentRequest, @CurrentUser Long employeeId);

    AchievementCommentDto updateComment(Long commentId, UpdateCommentRequest commentRequest, @CurrentUser Long employeeId);

    void deleteComment(Long commentId, @CurrentUser Long employeeId);

    void deleteCommentByHR(Long commentId, @CurrentUser Long hrEmployeeId, ModerationRequest moderationRequest);

    Long getCommentCount(Long postId);

    List<AchievementCommentDto> getCommentsForPost(Long postId);

    List<AchievementPostDto> searchPosts(String keyword, @CurrentUser Long currentEmployeeId);

    List<AchievementPostDto> filterPostsByAuthor(Long authorId, @CurrentUser Long currentEmployeeId);

    List<AchievementPostDto> filterPostsByDateRange(LocalDateTime startDate, LocalDateTime endDate,
            @CurrentUser Long currentEmployeeId);

}
