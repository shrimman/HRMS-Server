package intern.roima.hrmsbackend.services.Achievement_Module.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intern.roima.hrmsbackend.dtos.Requests.CreateCommentRequest;
import intern.roima.hrmsbackend.dtos.Requests.CreatePostRequest;
import intern.roima.hrmsbackend.dtos.Requests.ModerationRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateCommentRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdatePostRequest;
import intern.roima.hrmsbackend.dtos.Responses.AchievementCommentDto;
import intern.roima.hrmsbackend.dtos.Responses.AchievementLikeDto;
import intern.roima.hrmsbackend.dtos.Responses.AchievementPostDto;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.entities.Achievement_Module.AchievementComments;
import intern.roima.hrmsbackend.entities.Achievement_Module.AchievementLikes;
import intern.roima.hrmsbackend.entities.Achievement_Module.AchievementModerations;
import intern.roima.hrmsbackend.entities.Achievement_Module.AchievementPosts;
import intern.roima.hrmsbackend.entities.Achievement_Module.ModerationTypes;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.exceptions.InvalidEmployeeDataException;
import intern.roima.hrmsbackend.repositories.Achievement_Module.CommentRepository;
import intern.roima.hrmsbackend.repositories.Achievement_Module.LikeRepository;
import intern.roima.hrmsbackend.repositories.Achievement_Module.ModerationRepository;
import intern.roima.hrmsbackend.repositories.Achievement_Module.ModerationTypeRepository;
import intern.roima.hrmsbackend.repositories.Achievement_Module.PostRepository;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Achievement_Module.AchievementFeedService;
import intern.roima.hrmsbackend.services.Message_Module.NotificationService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class AchievementFeedServiceimpl implements AchievementFeedService {

    private static final Logger logger = LoggerFactory.getLogger(AchievementFeedServiceimpl.class);

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final EmployeeRepository employeeRepository;
    private final ModerationRepository moderationRepository;
    private final ModerationTypeRepository moderationTypeRepository;
    private final NotificationService notificationService;

    public AchievementFeedServiceimpl(
            PostRepository postRepository,
            CommentRepository commentRepository,
            LikeRepository likeRepository,
            EmployeeRepository employeeRepository,
            ModerationRepository moderationRepository,
            ModerationTypeRepository moderationTypeRepository,
            NotificationService notificationService) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.employeeRepository = employeeRepository;
        this.moderationRepository = moderationRepository;
        this.moderationTypeRepository = moderationTypeRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementPostDto> getAllPosts(@CurrentUser Long currentEmployeeId) {
        logger.info("Fetching all posts for employee ID: {}", currentEmployeeId);

        try {
            List<AchievementPosts> posts = postRepository.findAllByIsDeletedFalseOrderByCreatedAtDesc();
            return posts.stream()
                    .map(post -> toPostDto(post, currentEmployeeId))
                    .toList();
        } catch (DataAccessException e) {
            logger.error("Database error fetching all posts: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AchievementPostDto getPostById(Long postId, @CurrentUser Long currentEmployeeId) {
        logger.info("Fetching post ID: {} for employee ID: {}", postId, currentEmployeeId);

        try {
            AchievementPosts post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

            return toPostDto(post, currentEmployeeId);
        } catch (EntityNotFoundException e) {
            logger.error("Error fetching post ID {}: {}", postId, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error fetching post ID {}: {}", postId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementPostDto> getMyPosts(@CurrentUser Long employeeId) {
        logger.info("Fetching posts for employee ID: {}", employeeId);

        try {
            List<AchievementPosts> posts = postRepository.findByAuthor_EmployeeIdAndIsDeletedFalse(employeeId);
            return posts.stream()
                    .map(post -> toPostDto(post, employeeId))
                    .toList();
        } catch (DataAccessException e) {
            logger.error("Database error fetching posts for employee ID {}: {}", employeeId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementPostDto> getSystemGeneratedPosts(@CurrentUser Long currentEmployeeId) {
        logger.info("Fetching system-generated posts");

        try {
            List<AchievementPosts> posts = postRepository.findByIsSystemGeneratedAndIsDeletedFalse(true);
            return posts.stream()
                    .map(post -> toPostDto(post, currentEmployeeId))
                    .toList();
        } catch (DataAccessException e) {
            logger.error("Database error fetching system-generated posts: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public AchievementPostDto createPost(CreatePostRequest postRequest, @CurrentUser Long authorId) {
        logger.info("Creating post for author ID: {}", authorId);

        try {
            Employees author = employeeRepository.findById(authorId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + authorId));

            AchievementPosts post = new AchievementPosts();
            post.setAuthor(author);
            post.setTitle(postRequest.getTitle().trim());
            post.setDescription(postRequest.getDescription() != null ? postRequest.getDescription().trim() : null);
            post.setIsSystemGenerated(false);
            post.setIsDeleted(false);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            post.setUpdatedByEmployee(author);

            System.out.println("before save: " + post);
            AchievementPosts savedPost = postRepository.save(post);
            System.out.println(savedPost);
            logger.info("Successfully created post ID: {} by employee ID: {}", savedPost.getPostId(), authorId);

            return toPostDto(savedPost, authorId);
        } catch (EntityNotFoundException e) {
            logger.error("Error creating post: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error creating post: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public AchievementPostDto updatePost(Long postId, UpdatePostRequest postRequest, @CurrentUser Long employeeId) {
        logger.info("Updating post ID: {} by employee ID: {}", postId, employeeId);

        try {
            AchievementPosts post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

            if (!post.getAuthor().getEmployeeId().equals(employeeId)) {
                throw new InvalidEmployeeDataException("You can only update your own posts");
            }

            post.setTitle(postRequest.getTitle().trim());
            post.setDescription(postRequest.getDescription() != null ? postRequest.getDescription().trim() : null);
            post.setUpdatedAt(LocalDateTime.now());
            post.setUpdatedByEmployee(post.getAuthor());

            AchievementPosts savedPost = postRepository.save(post);
            logger.info("Successfully updated post ID: {}", postId);

            return toPostDto(savedPost, employeeId);
        } catch (EntityNotFoundException | InvalidEmployeeDataException e) {
            logger.error("Error updating post ID {}: {}", postId, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error updating post ID {}: {}", postId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deletePost(Long postId, @CurrentUser Long employeeId) {
        logger.info("Deleting post ID: {} by employee ID: {}", postId, employeeId);

        try {
            AchievementPosts post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

            if (!post.getAuthor().getEmployeeId().equals(employeeId)) {
                throw new InvalidEmployeeDataException("You can only delete your own posts");
            }

            post.setIsDeleted(true);
            post.setUpdatedAt(LocalDateTime.now());
            postRepository.save(post);
            logger.info("Successfully deleted post ID: {}", postId);
        } catch (EntityNotFoundException | InvalidEmployeeDataException e) {
            logger.error("Error deleting post ID {}: {}", postId, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error deleting post ID {}: {}", postId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deletePostByHR(Long postId, @CurrentUser Long hrEmployeeId, ModerationRequest moderationRequest) {
        logger.info("HR employee ID: {} deleting post ID: {}", hrEmployeeId, postId);

        try {
            AchievementPosts post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

            if (post.getIsDeleted()) {
                throw new IllegalArgumentException("Post is already deleted");
            }

            Employees hrEmployee = employeeRepository.findById(hrEmployeeId)
                    .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrEmployeeId));

            ModerationTypes moderationType = moderationTypeRepository.findById(moderationRequest.getModerationTypeId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Moderation type not found with ID: " + moderationRequest.getModerationTypeId()));

            AchievementModerations moderation = new AchievementModerations();
            moderation.setPost(post);
            moderation.setModerationType(moderationType);
            moderation.setDeletedBy(hrEmployee);
            moderation.setReason(moderationRequest.getReason());
            moderation.setDeletedAt(LocalDateTime.now());

            moderationRepository.save(moderation);

            post.setIsDeleted(true);
            post.setUpdatedAt(LocalDateTime.now());
            postRepository.save(post);

            notificationService.sendWarningNotification(
                    post.getAuthor().getEmployeeId(),
                    moderationRequest.getReason(),
                    postId);

            logger.info("Successfully deleted post ID: {} by HR with moderation", postId);
        } catch (EntityNotFoundException e) {
            logger.error("Error in HR post deletion: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error in HR post deletion: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in HR post deletion: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public AchievementLikeDto likePost(Long postId, @CurrentUser Long employeeId) {
        logger.info("Employee ID: {} liking post ID: {}", employeeId, postId);

        try {
            if (likeRepository.existsByPost_PostIdAndEmployee_EmployeeId(postId, employeeId)) {
                throw new InvalidEmployeeDataException("You have already liked this post");
            }

            AchievementPosts post = postRepository.findById(postId)
                    .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

            Employees employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            AchievementLikes like = new AchievementLikes();
            like.setPost(post);
            like.setEmployee(employee);
            like.setCreatedAt(LocalDateTime.now());
            like.setUpdatedAt(LocalDateTime.now());
            like.setUpdatedByEmployee(employee);

            AchievementLikes savedLike = likeRepository.save(like);
            logger.info("Successfully liked post ID: {} by employee ID: {}", postId, employeeId);

            return toLikeDto(savedLike);
        } catch (EntityNotFoundException | InvalidEmployeeDataException e) {
            logger.error("Error liking post ID {}: {}", postId, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error liking post ID {}: {}", postId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void unlikePost(Long postId, @CurrentUser Long employeeId) {
        logger.info("Employee ID: {} unliking post ID: {}", employeeId, postId);

        try {
            AchievementLikes like = likeRepository.findByPost_PostIdAndEmployee_EmployeeId(postId, employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Like not found for this post and employee"));

            likeRepository.delete(like);
            logger.info("Successfully unliked post ID: {} by employee ID: {}", postId, employeeId);
        } catch (EntityNotFoundException e) {
            logger.error("Error unliking post ID {}: {}", postId, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error unliking post ID {}: {}", postId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserLikedPost(Long postId, @CurrentUser Long employeeId) {
        logger.info("Checking if employee ID: {} liked post ID: {}", employeeId, postId);

        try {
            return likeRepository.existsByPost_PostIdAndEmployee_EmployeeId(postId, employeeId);
        } catch (DataAccessException e) {
            logger.error("Database error checking like status: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLikeCount(Long postId) {
        logger.info("Fetching like count for post ID: {}", postId);

        try {
            return likeRepository.countByPost_PostId(postId);
        } catch (DataAccessException e) {
            logger.error("Database error fetching like count: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementLikeDto> getLikesForPost(Long postId) {
        logger.info("Fetching likes for post ID: {}", postId);

        try {
            List<AchievementLikes> likes = likeRepository.findByPost_PostId(postId);
            return likes.stream()
                    .map(this::toLikeDto)
                    .toList();
        } catch (DataAccessException e) {
            logger.error("Database error fetching likes: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public AchievementCommentDto addComment(CreateCommentRequest commentRequest, @CurrentUser Long employeeId) {
        logger.info("Employee ID: {} adding comment to post ID: {}", employeeId, commentRequest.getPostId());

        try {
            AchievementPosts post = postRepository.findById(commentRequest.getPostId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("Post not found with ID: " + commentRequest.getPostId()));

            Employees employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            AchievementComments comment = new AchievementComments();
            comment.setPost(post);
            comment.setAuthor(employee);
            comment.setText(commentRequest.getText().trim());
            comment.setIsDeleted(false);
            comment.setCreatedAt(LocalDateTime.now());
            comment.setUpdatedAt(LocalDateTime.now());
            comment.setUpdatedByEmployee(employee);

            AchievementComments savedComment = commentRepository.save(comment);
            logger.info("Successfully added comment ID: {} to post ID: {}", savedComment.getCommentId(),
                    commentRequest.getPostId());

            return toCommentDto(savedComment);
        } catch (EntityNotFoundException e) {
            logger.error("Error adding comment: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error adding comment: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public AchievementCommentDto updateComment(Long commentId, UpdateCommentRequest commentRequest,
            @CurrentUser Long employeeId) {
        logger.info("Updating comment ID: {} by employee ID: {}", commentId, employeeId);

        try {
            AchievementComments comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));

            if (!comment.getAuthor().getEmployeeId().equals(employeeId)) {
                throw new InvalidEmployeeDataException("You can only update your own comments");
            }

            comment.setText(commentRequest.getText().trim());
            comment.setUpdatedAt(LocalDateTime.now());
            comment.setUpdatedByEmployee(comment.getAuthor());

            AchievementComments savedComment = commentRepository.save(comment);
            logger.info("Successfully updated comment ID: {}", commentId);

            return toCommentDto(savedComment);
        } catch (EntityNotFoundException | InvalidEmployeeDataException e) {
            logger.error("Error updating comment ID {}: {}", commentId, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error updating comment ID {}: {}", commentId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, @CurrentUser Long employeeId) {
        logger.info("Deleting comment ID: {} by employee ID: {}", commentId, employeeId);

        try {
            AchievementComments comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));

            if (!comment.getAuthor().getEmployeeId().equals(employeeId)) {
                throw new InvalidEmployeeDataException("You can only delete your own comments");
            }

            comment.setIsDeleted(true);
            comment.setUpdatedAt(LocalDateTime.now());
            commentRepository.save(comment);
            logger.info("Successfully deleted comment ID: {}", commentId);
        } catch (EntityNotFoundException | InvalidEmployeeDataException e) {
            logger.error("Error deleting comment ID {}: {}", commentId, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error deleting comment ID {}: {}", commentId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteCommentByHR(Long commentId, @CurrentUser Long hrEmployeeId,
            ModerationRequest moderationRequest) {
        logger.info("HR employee ID: {} deleting comment ID: {}", hrEmployeeId, commentId);

        try {
            AchievementComments comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new EntityNotFoundException("Comment not found with ID: " + commentId));

            if (comment.getIsDeleted()) {
                throw new IllegalArgumentException("Comment is already deleted");
            }

            Employees hrEmployee = employeeRepository.findById(hrEmployeeId)
                    .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrEmployeeId));

            ModerationTypes moderationType = moderationTypeRepository.findById(moderationRequest.getModerationTypeId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Moderation type not found with ID: " + moderationRequest.getModerationTypeId()));

            AchievementModerations moderation = new AchievementModerations();
            moderation.setComment(comment);
            moderation.setModerationType(moderationType);
            moderation.setDeletedBy(hrEmployee);
            moderation.setReason(moderationRequest.getReason());
            moderation.setDeletedAt(LocalDateTime.now());

            moderationRepository.save(moderation);

            comment.setIsDeleted(true);
            comment.setUpdatedAt(LocalDateTime.now());
            commentRepository.save(comment);

            notificationService.sendWarningNotification(comment.getAuthor().getEmployeeId(),
                    moderationRequest.getReason(), commentId);
            logger.info("Successfully deleted comment ID: {} by HR with moderation", commentId);
        } catch (EntityNotFoundException e) {
            logger.error("Error in HR comment deletion: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error in HR comment deletion: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCommentCount(Long postId) {
        logger.info("Fetching comment count for post ID: {}", postId);

        try {
            return commentRepository.countByPost_PostIdAndIsDeletedFalse(postId);
        } catch (DataAccessException e) {
            logger.error("Database error fetching comment count: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementCommentDto> getCommentsForPost(Long postId) {
        logger.info("Fetching comments for post ID: {}", postId);

        try {
            List<AchievementComments> comments = commentRepository
                    .findByPost_PostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId);
            return comments.stream()
                    .map(this::toCommentDto)
                    .toList();
        } catch (DataAccessException e) {
            logger.error("Database error fetching comments: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementPostDto> searchPosts(String keyword, @CurrentUser Long currentEmployeeId) {
        logger.info("Searching posts with keyword: {}", keyword);

        try {
            List<AchievementPosts> posts = postRepository.searchByKeyword(keyword);
            return posts.stream()
                    .map(post -> toPostDto(post, currentEmployeeId))
                    .toList();
        } catch (DataAccessException e) {
            logger.error("Database error searching posts: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementPostDto> filterPostsByAuthor(Long authorId, @CurrentUser Long currentEmployeeId) {
        logger.info("Filtering posts by author ID: {}", authorId);

        try {
            List<AchievementPosts> posts = postRepository.findByAuthor_EmployeeIdAndIsDeletedFalse(authorId);
            return posts.stream()
                    .map(post -> toPostDto(post, currentEmployeeId))
                    .toList();
        } catch (DataAccessException e) {
            logger.error("Database error filtering posts by author: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AchievementPostDto> filterPostsByDateRange(LocalDateTime startDate, LocalDateTime endDate,
            @CurrentUser Long currentEmployeeId) {
        logger.info("Filtering posts between {} and {}", startDate, endDate);

        try {
            List<AchievementPosts> posts = postRepository.findByCreatedAtBetweenAndIsDeletedFalse(startDate, endDate);
            return posts.stream()
                    .map(post -> toPostDto(post, currentEmployeeId))
                    .toList();
        } catch (DataAccessException e) {
            logger.error("Database error filtering posts by date: {}", e.getMessage());
            throw e;
        }
    }

    private AchievementPostDto toPostDto(AchievementPosts post, Long currentEmployeeId) {
        System.out.println("POST AUTHOR : " + post.getAuthor());
        AchievementPostDto dto = new AchievementPostDto();

        dto.setPostId(post.getPostId());
        dto.setTitle(post.getTitle());
        dto.setDescription(post.getDescription());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setIsSystemGenerated(post.getIsSystemGenerated());
        dto.setAuthor(toEmployeeSummaryDto(post.getAuthor()));
        dto.setLikeCount(likeRepository.countByPost_PostId(post.getPostId()));
        dto.setCommentCount(commentRepository.countByPost_PostIdAndIsDeletedFalse(post.getPostId()));

        dto.setIsLikedByCurrentUser(
                likeRepository.existsByPost_PostIdAndEmployee_EmployeeId(post.getPostId(), currentEmployeeId));
        System.out.println(dto);
        return dto;
    }

    private AchievementCommentDto toCommentDto(AchievementComments comment) {
        AchievementCommentDto dto = new AchievementCommentDto();
        dto.setCommentId(comment.getCommentId());
        dto.setPostId(comment.getPost().getPostId());
        dto.setAuthor(toEmployeeSummaryDto(comment.getAuthor()));
        dto.setText(comment.getText());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        return dto;
    }

    private AchievementLikeDto toLikeDto(AchievementLikes like) {
        AchievementLikeDto dto = new AchievementLikeDto();
        dto.setLikeId(like.getLikeId());
        dto.setPostId(like.getPost().getPostId());
        dto.setEmployee(toEmployeeSummaryDto(like.getEmployee()));
        dto.setCreatedAt(like.getCreatedAt());
        return dto;
    }

    private EmployeeSummaryDto toEmployeeSummaryDto(Employees employee) {
        EmployeeSummaryDto dto = new EmployeeSummaryDto();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setRole(employee.getRole());
        dto.setDepartment(employee.getDepartment());
        dto.setDesignation(employee.getDesignation());
        dto.setActive(employee.isActive());
        dto.setPhotoPath(employee.getPhotoPath());
        dto.setDateOfBirth(employee.getDateOfBirth());
        dto.setDateOfJoining(employee.getDateOfJoining());

        if (employee.getManager() != null) {
            dto.setManagerId(employee.getManager().getEmployeeId());
            dto.setManagerName(employee.getManager().getFirstName() + " " + employee.getManager().getLastName());
        }
        return dto;
    }

}
