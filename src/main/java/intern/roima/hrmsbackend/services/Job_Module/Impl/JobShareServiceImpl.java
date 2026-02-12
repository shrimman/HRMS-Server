package intern.roima.hrmsbackend.services.Job_Module.Impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intern.roima.hrmsbackend.dtos.Requests.ShareJobRequest;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.JobOpeningDto;
import intern.roima.hrmsbackend.dtos.Responses.JobShareLogDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.entities.Job_Module.JobOpenings;
import intern.roima.hrmsbackend.entities.Job_Module.JobShareLog;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.repositories.Job_Module.JobOpeningRepository;
import intern.roima.hrmsbackend.repositories.Job_Module.JobShareRepository;
import intern.roima.hrmsbackend.services.Job_Module.JobShareService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class JobShareServiceImpl implements JobShareService {

    private static final Logger logger = LoggerFactory.getLogger(JobShareServiceImpl.class);

    private final JobShareRepository jobShareRepository;
    private final JobOpeningRepository jobOpeningRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    public JobShareServiceImpl(
            JobShareRepository jobShareRepository,
            JobOpeningRepository jobOpeningRepository,
            EmployeeRepository employeeRepository,
            ModelMapper modelMapper) {
        this.jobShareRepository = jobShareRepository;
        this.jobOpeningRepository = jobOpeningRepository;
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobShareLogDto> getShareLogsForJob(Long jobId) {
        logger.info("Fetching share logs for job ID: {}", jobId);

        try {
            List<JobShareLog> shareLogs = jobShareRepository.findByJobOpening_JobId(jobId);
            List<JobShareLogDto> result = shareLogs.stream()
                    .map(this::mapToDto)
                    .toList();

            logger.debug("Found {} share logs for job ID: {}", result.size(), jobId);
            return result;

        } catch (DataAccessException e) {
            logger.error("Database error fetching share logs for job: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching job share logs", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobShareLogDto> getMyShareHistory(Long employeeId) {
        logger.info("Fetching share history for employee ID: {}", employeeId);

        try {
            List<JobShareLog> shareLogs = jobShareRepository.findBySharedBy_EmployeeId(employeeId);
            List<JobShareLogDto> result = shareLogs.stream()
                    .map(this::mapToDto)
                    .toList();

            logger.debug("Found {} share logs for employee ID: {}", result.size(), employeeId);
            return result;

        } catch (DataAccessException e) {
            logger.error("Database error fetching share history for employee: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching employee share history", e);
        }
    }

    private JobShareLogDto mapToDto(JobShareLog jobShareLog) {
        JobShareLogDto dto = modelMapper.map(jobShareLog, JobShareLogDto.class);

        JobOpeningDto jobOpeningDto = modelMapper.map(jobShareLog.getJobOpening(), JobOpeningDto.class);
        jobOpeningDto.setJobHROwnerId(jobShareLog.getJobOpening().getJobHROwner().getEmployeeId());
        dto.setJobOpening(jobOpeningDto);

        EmployeeSummaryDto sharedByDto = modelMapper.map(jobShareLog.getSharedBy(), EmployeeSummaryDto.class);
        dto.setSharedBy(sharedByDto);

        return dto;
    }

    @Override
    @Transactional
    public List<JobShareLogDto> shareJob(Long jobId, ShareJobRequest shareRequest, Long employeeId) {
        logger.info("Sharing job ID: {} to {} recipients by employee ID: {}",
                jobId, shareRequest.getRecipientEmails().size(), employeeId);

        try {
            JobOpenings jobOpening = jobOpeningRepository.findById(jobId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Job opening not found with ID: " + jobId));

            Employees sharingEmployee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Sharing employee not found with ID: " + employeeId));

            List<JobShareLogDto> shareLogs = new ArrayList<>();
            LocalDateTime sharedAt = LocalDateTime.now();

            for (String recipientEmail : shareRequest.getRecipientEmails()) {
                JobShareLog shareLog = new JobShareLog();
                shareLog.setJobOpening(jobOpening);
                shareLog.setSharedBy(sharingEmployee);
                shareLog.setRecipientEmail(recipientEmail);
                shareLog.setSharedAt(sharedAt);

                JobShareLog savedLog = jobShareRepository.save(shareLog);
                shareLogs.add(mapToDto(savedLog));

                logger.debug("Logged job share to: {}", recipientEmail);
            }

            // TODO: Send email to recipients with job title, summary, and JD file attachment if available

            logger.info("Successfully shared job ID: {} with {} recipients", jobId, shareLogs.size());

            return shareLogs;

        } catch (EntityNotFoundException e) {
            logger.error("Error sharing job: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error sharing job: {}", e.getMessage());
            throw new RuntimeException("Database error while sharing job", e);
        }
    }

}
