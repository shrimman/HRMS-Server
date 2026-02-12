package intern.roima.hrmsbackend.services.Job_Module.Impl;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intern.roima.hrmsbackend.dtos.Requests.CreateJobRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateJobRequest;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.JobOpeningDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.entities.Job_Module.JobOpenings;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.repositories.Job_Module.JobOpeningRepository;
import intern.roima.hrmsbackend.services.Job_Module.JobOpeningService;
import intern.roima.hrmsbackend.services.Utils.FileStorageService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class JobOpeningServiceImpl implements JobOpeningService {

    private static final Logger logger = LoggerFactory.getLogger(JobOpeningServiceImpl.class);

    private final JobOpeningRepository jobOpeningRepository;
    private final EmployeeRepository employeeRepository;
    private final FileStorageService fileStorageService;
    private final ModelMapper modelMapper;

    public JobOpeningServiceImpl(
            JobOpeningRepository jobOpeningRepository,
            EmployeeRepository employeeRepository,
            FileStorageService fileStorageService,
            ModelMapper modelMapper) {
        this.jobOpeningRepository = jobOpeningRepository;
        this.employeeRepository = employeeRepository;
        this.fileStorageService = fileStorageService;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobOpeningDto> getAllActiveJobs() {
        logger.info("Fetching all active job openings");
        List<JobOpenings> activeJobs = jobOpeningRepository.findByIsActiveTrue();
        return activeJobs.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobOpeningDto> getAllJobs() {
        logger.info("Fetching all job openings");
        List<JobOpenings> allJobs = jobOpeningRepository.findAll();
        return allJobs.stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JobOpeningDto getJobById(Long jobId) {
        logger.info("Fetching job opening with ID: {}", jobId);
        JobOpenings jobOpening = jobOpeningRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job opening not found with ID: " + jobId));
        return mapToDto(jobOpening);
    }

    @Override
    @Transactional
    public JobOpeningDto createJob(CreateJobRequest jobRequest, Long hrId) {
        logger.info("Creating new job opening with title: {}", jobRequest.getTitle());

        Employees hrOwner = employeeRepository.findById(
                jobRequest.getJobHROwnerId() != null ? jobRequest.getJobHROwnerId() : hrId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "HR owner not found with ID: " + (jobRequest.getJobHROwnerId() != null
                                ? jobRequest.getJobHROwnerId()
                                : hrId)));

        JobOpenings jobOpening = new JobOpenings();
        jobOpening.setTitle(jobRequest.getTitle());
        jobOpening.setSummary(jobRequest.getSummary());
        jobOpening.setIsActive(true);
        jobOpening.setJobHROwner(hrOwner);
        jobOpening.setPostedAt(LocalDateTime.now());
        jobOpening.setUpdatedAt(LocalDateTime.now());
        jobOpening.setUpdatedByEmployee(hrOwner);

        JobOpenings savedJob = jobOpeningRepository.save(jobOpening);
        logger.info("Successfully created job opening with ID: {}", savedJob.getJobId());

        if (jobRequest.getJdFile() != null && !jobRequest.getJdFile().isEmpty()) {
            String jdFilePath = fileStorageService.storeJobDescription(jobRequest.getJdFile(), savedJob.getJobId());
            savedJob.setJdFilePath(jdFilePath);
            savedJob = jobOpeningRepository.save(savedJob);
            logger.info("Successfully stored JD file for job ID: {}", savedJob.getJobId());
        }

        return mapToDto(savedJob);
    }

    @Override
    @Transactional
    public JobOpeningDto updateJob(Long jobId, UpdateJobRequest jobRequest, Long hrId) {
        logger.info("Updating job opening with ID: {}", jobId);

        JobOpenings jobOpening = jobOpeningRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job opening not found with ID: " + jobId));

        Employees updatingEmployee = employeeRepository.findById(hrId)
                .orElseThrow(() -> new EntityNotFoundException("Updating employee not found with ID: " + hrId));

        System.out.println("New Title: " + jobRequest.getTitle());
        if (jobRequest.getTitle() != null) {
            jobOpening.setTitle(jobRequest.getTitle());
        }
        if (jobRequest.getSummary() != null) {
            jobOpening.setSummary(jobRequest.getSummary());
        }
        if (jobRequest.getJobHROwnerId() != null) {
            Employees newHrOwner = employeeRepository.findById(jobRequest.getJobHROwnerId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "HR owner not found with ID: " + jobRequest.getJobHROwnerId()));
            jobOpening.setJobHROwner(newHrOwner);
        }

        if (jobRequest.getJdFile() != null && !jobRequest.getJdFile().isEmpty()) {
            if (jobOpening.getJdFilePath() != null) {
                fileStorageService.deleteFile(jobOpening.getJdFilePath());
                logger.info("Deleted old JD file for job ID: {}", jobId);
            }
            String newJdFilePath = fileStorageService.storeJobDescription(jobRequest.getJdFile(), jobId);
            jobOpening.setJdFilePath(newJdFilePath);
            logger.info("Successfully stored new JD file for job ID: {}", jobId);
        }

        jobOpening.setUpdatedAt(LocalDateTime.now());
        jobOpening.setUpdatedByEmployee(updatingEmployee);

        JobOpenings updatedJob = jobOpeningRepository.save(jobOpening);
        logger.info("Successfully updated job opening with ID: {}", jobId);

        return mapToDto(updatedJob);
    }

    @Override
    @Transactional
    public void deactivateJob(Long jobId, Long hrId) {
        logger.info("Deactivating job opening with ID: {}", jobId);

        JobOpenings jobOpening = jobOpeningRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job opening not found with ID: " + jobId));

        Employees updatingEmployee = employeeRepository.findById(hrId)
                .orElseThrow(() -> new EntityNotFoundException("Updating employee not found with ID: " + hrId));

        jobOpening.setIsActive(false);
        jobOpening.setUpdatedAt(LocalDateTime.now());
        jobOpening.setUpdatedByEmployee(updatingEmployee);

        jobOpeningRepository.save(jobOpening);
        logger.info("Successfully deactivated job opening with ID: {}", jobId);
    }

    @Override
    @Transactional
    public void activateJob(Long jobId, Long hrId) {
        logger.info("Activating job opening with ID: {}", jobId);

        JobOpenings jobOpening = jobOpeningRepository.findById(jobId)
                .orElseThrow(() -> new EntityNotFoundException("Job opening not found with ID: " + jobId));

        Employees updatingEmployee = employeeRepository.findById(hrId)
                .orElseThrow(() -> new EntityNotFoundException("Updating employee not found with ID: " + hrId));

        jobOpening.setIsActive(true);
        jobOpening.setUpdatedAt(LocalDateTime.now());
        jobOpening.setUpdatedByEmployee(updatingEmployee);

        jobOpeningRepository.save(jobOpening);
        logger.info("Successfully activated job opening with ID: {}", jobId);
    }

    private JobOpeningDto mapToDto(JobOpenings jobOpening) {
        JobOpeningDto dto = modelMapper.map(jobOpening, JobOpeningDto.class);
        dto.setJobHROwnerId(jobOpening.getJobHROwner().getEmployeeId());

        if (jobOpening.getUpdatedByEmployee() != null) {
            EmployeeSummaryDto updatedByDto = modelMapper.map(
                    jobOpening.getUpdatedByEmployee(),
                    EmployeeSummaryDto.class);
            dto.setUpdatedByEmployee(updatedByDto);
        }

        return dto;
    }
}
