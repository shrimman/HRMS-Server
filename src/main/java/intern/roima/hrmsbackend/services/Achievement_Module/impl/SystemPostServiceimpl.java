package intern.roima.hrmsbackend.services.Achievement_Module.impl;

import java.time.LocalDateTime;
import java.time.Period;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intern.roima.hrmsbackend.dtos.Responses.AchievementPostDto;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.entities.Achievement_Module.AchievementPosts;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.repositories.Achievement_Module.CommentRepository;
import intern.roima.hrmsbackend.repositories.Achievement_Module.LikeRepository;
import intern.roima.hrmsbackend.repositories.Achievement_Module.PostRepository;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.services.Achievement_Module.SystemPostService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class SystemPostServiceimpl implements SystemPostService {

    private static final Logger logger = LoggerFactory.getLogger(SystemPostServiceimpl.class);

    private final PostRepository postRepository;
    private final EmployeeRepository employeeRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ModelMapper modelMapper;

    public SystemPostServiceimpl(
            PostRepository postRepository,
            EmployeeRepository employeeRepository,
            LikeRepository likeRepository,
            CommentRepository commentRepository,
            ModelMapper modelMapper) {
        this.postRepository = postRepository;
        this.employeeRepository = employeeRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public AchievementPostDto createBirthdayPost(Long employeeId) {
        logger.info("Creating birthday post for employee ID: {}", employeeId);

        try {
            Employees employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            String title = String.format("Happy Birthday %s %s!", employee.getFirstName(), employee.getLastName());
            String description = String.format("Today is %s %s's birthday! Let's wish them a wonderful day ahead!",
                    employee.getFirstName(), employee.getLastName());

            AchievementPosts post = new AchievementPosts();
            post.setAuthor(employee);
            post.setTitle(title);
            post.setDescription(description);
            post.setIsSystemGenerated(true);
            post.setIsDeleted(false);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            post.setUpdatedByEmployee(employee);

            AchievementPosts savedPost = postRepository.save(post);
            logger.info("Successfully created birthday post ID: {} for employee ID: {}", savedPost.getPostId(),
                    employeeId);

            return toPostDto(savedPost);
        } catch (EntityNotFoundException e) {
            logger.error("Error creating birthday post: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error creating birthday post: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public AchievementPostDto createWorkAnniversaryPost(Long employeeId) {
        logger.info("Creating work anniversary post for employee ID: {}", employeeId);

        try {
            Employees employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

            if (employee.getDateOfJoining() == null) {
                throw new IllegalStateException("Employee joining date is not available");
            }

            int years = Period.between(employee.getDateOfJoining(), LocalDateTime.now().toLocalDate()).getYears();

            String title = String.format("Work Anniversary - %s %s!", employee.getFirstName(),
                    employee.getLastName());
            String description = String.format("%s %s completes %d %s at the organization today! Congratulations!",
                    employee.getFirstName(), employee.getLastName(), years, years == 1 ? "year" : "years");

            AchievementPosts post = new AchievementPosts();
            post.setAuthor(employee);
            post.setTitle(title);
            post.setDescription(description);
            post.setIsSystemGenerated(true);
            post.setIsDeleted(false);
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            post.setUpdatedByEmployee(employee);

            AchievementPosts savedPost = postRepository.save(post);
            logger.info("Successfully created work anniversary post ID: {} for employee ID: {}",
                    savedPost.getPostId(), employeeId);

            return toPostDto(savedPost);
        } catch (EntityNotFoundException | IllegalStateException e) {
            logger.error("Error creating work anniversary post: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error creating work anniversary post: {}", e.getMessage());
            throw e;
        }
    }

    private AchievementPostDto toPostDto(AchievementPosts post) {
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
        dto.setIsLikedByCurrentUser(false);
        return dto;
    }

    private EmployeeSummaryDto toEmployeeSummaryDto(Employees employee) {
        EmployeeSummaryDto dto = modelMapper.map(employee, EmployeeSummaryDto.class);

        if (employee.getManager() != null) {
            dto.setManagerId(employee.getManager().getEmployeeId());
            dto.setManagerName(employee.getManager().getFirstName() + " " + employee.getManager().getLastName());
        }
        return dto;
    }

}
