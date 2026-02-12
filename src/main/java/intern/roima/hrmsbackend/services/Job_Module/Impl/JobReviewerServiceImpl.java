package intern.roima.hrmsbackend.services.Job_Module.Impl;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.JobOpeningDto;
import intern.roima.hrmsbackend.dtos.Responses.JobReviewerDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.entities.Job_Module.JobOpenings;
import intern.roima.hrmsbackend.entities.Job_Module.JobReviewers;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.repositories.Job_Module.JobOpeningRepository;
import intern.roima.hrmsbackend.repositories.Job_Module.JobReviewerRepository;
import intern.roima.hrmsbackend.services.Job_Module.JobReviewerService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class JobReviewerServiceImpl implements JobReviewerService {

    private static final Logger logger = LoggerFactory.getLogger(JobReviewerServiceImpl.class);

    private final JobReviewerRepository jobReviewerRepository;
    private final JobOpeningRepository jobOpeningRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    public JobReviewerServiceImpl(
            JobReviewerRepository jobReviewerRepository,
            JobOpeningRepository jobOpeningRepository,
            EmployeeRepository employeeRepository,
            ModelMapper modelMapper) {
        this.jobReviewerRepository = jobReviewerRepository;
        this.jobOpeningRepository = jobOpeningRepository;
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public JobReviewerDto assignReviewer(Long jobId, Long reviewerId, Long assignedBy) {
        logger.info("Assigning reviewer ID: {} to job ID: {} by employee ID: {}", reviewerId, jobId, assignedBy);

        try {
            JobOpenings jobOpening = jobOpeningRepository.findById(jobId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Job opening not found with ID: " + jobId));

            Employees reviewer = employeeRepository.findById(reviewerId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Reviewer employee not found with ID: " + reviewerId));

            Employees assignedByEmployee = employeeRepository.findById(assignedBy)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Assigning employee not found with ID: " + assignedBy));

            JobReviewers jobReviewer = new JobReviewers();
            jobReviewer.setJobOpening(jobOpening);
            jobReviewer.setReviewer(reviewer);
            jobReviewer.setAssignedBy(assignedByEmployee);
            jobReviewer.setAssignedAt(LocalDateTime.now());
            jobReviewer.setUpdatedAt(LocalDateTime.now());
            jobReviewer.setUpdatedByEmployee(assignedByEmployee);

            JobReviewers savedJobReviewer = jobReviewerRepository.save(jobReviewer);
            logger.info("Successfully assigned reviewer with job reviewer ID: {}", savedJobReviewer.getReviewerId());

            return mapToDto(savedJobReviewer);

        } catch (EntityNotFoundException e) {
            logger.error("Error assigning reviewer: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error assigning reviewer: {}", e.getMessage());
            throw new RuntimeException("Database error while assigning reviewer", e);
        }
    }

    @Override
    @Transactional
    public void removeReviewer(Long jobReviewerId, Long hrId) {
        logger.info("Removing job reviewer with ID: {} by HR employee ID: {}", jobReviewerId, hrId);

        try {
            JobReviewers jobReviewer = jobReviewerRepository.findById(jobReviewerId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Job reviewer assignment not found with ID: " + jobReviewerId));

            Employees hrEmployee = employeeRepository.findById(hrId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "HR employee not found with ID: " + hrId));

            jobReviewer.setUpdatedByEmployee(hrEmployee);
            jobReviewer.setUpdatedAt(LocalDateTime.now());

            jobReviewerRepository.delete(jobReviewer);
            logger.info("Successfully removed job reviewer with ID: {}", jobReviewerId);

        } catch (EntityNotFoundException e) {
            logger.error("Error removing reviewer: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error removing reviewer: {}", e.getMessage());
            throw new RuntimeException("Database error while removing reviewer", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobReviewerDto> getReviewersForJob(Long jobId) {
        logger.info("Fetching reviewers for job ID: {}", jobId);

        try {
            List<JobReviewers> jobReviewers = jobReviewerRepository.findByJobOpening_JobId(jobId);
            List<JobReviewerDto> result = jobReviewers.stream()
                    .map(this::mapToDto)
                    .toList();

            logger.debug("Found {} reviewers for job ID: {}", result.size(), jobId);
            return result;

        } catch (DataAccessException e) {
            logger.error("Database error fetching reviewers for job: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching job reviewers", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobOpeningDto> getJobsForReviewer(Long reviewerId) {
        logger.info("Fetching jobs assigned to reviewer ID: {}", reviewerId);

        try {
            List<JobReviewers> jobReviewers = jobReviewerRepository.findByReviewer_EmployeeId(reviewerId);
            List<JobOpeningDto> result = jobReviewers.stream()
                    .map(jr -> {
                        JobOpeningDto dto = modelMapper.map(jr.getJobOpening(), JobOpeningDto.class);
                        dto.setJobHROwnerId(jr.getJobOpening().getJobHROwner().getEmployeeId());
                        return dto;
                    })
                    .toList();

            logger.debug("Found {} jobs for reviewer ID: {}", result.size(), reviewerId);
            return result;

        } catch (DataAccessException e) {
            logger.error("Database error fetching jobs for reviewer: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching reviewer jobs", e);
        }
    }

    private JobReviewerDto mapToDto(JobReviewers jobReviewer) {
        JobReviewerDto dto = modelMapper.map(jobReviewer, JobReviewerDto.class);

        JobOpeningDto jobOpeningDto = modelMapper.map(jobReviewer.getJobOpening(), JobOpeningDto.class);
        jobOpeningDto.setJobHROwnerId(jobReviewer.getJobOpening().getJobHROwner().getEmployeeId());
        dto.setJobOpening(jobOpeningDto);

        EmployeeSummaryDto reviewerDto = modelMapper.map(jobReviewer.getReviewer(), EmployeeSummaryDto.class);
        dto.setReviewer(reviewerDto);

        EmployeeSummaryDto assignedByDto = modelMapper.map(jobReviewer.getAssignedBy(), EmployeeSummaryDto.class);
        dto.setAssignedBy(assignedByDto);

        if (jobReviewer.getUpdatedByEmployee() != null) {
            EmployeeSummaryDto updatedByDto = modelMapper.map(
                    jobReviewer.getUpdatedByEmployee(),
                    EmployeeSummaryDto.class);
            dto.setUpdatedByEmployee(updatedByDto);
        }

        return dto;
    }
}
