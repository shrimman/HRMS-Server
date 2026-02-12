package intern.roima.hrmsbackend.services.Job_Module.Impl;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intern.roima.hrmsbackend.dtos.Requests.CreateReferralRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateReferralStatusRequest;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.JobOpeningDto;
import intern.roima.hrmsbackend.dtos.Responses.JobReferralDto;
import intern.roima.hrmsbackend.dtos.Responses.ReferralStatusDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.entities.Job_Module.JobOpenings;
import intern.roima.hrmsbackend.entities.Job_Module.ReferralStatus;
import intern.roima.hrmsbackend.entities.Job_Module.Referrals;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.repositories.Job_Module.JobOpeningRepository;
import intern.roima.hrmsbackend.repositories.Job_Module.ReferralRepository;
import intern.roima.hrmsbackend.repositories.Job_Module.ReferralStatusRepository;
import intern.roima.hrmsbackend.services.Job_Module.JobReferralService;
import intern.roima.hrmsbackend.services.Utils.FileStorageService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class JobReferralServiceImpl implements JobReferralService {

    private static final Logger logger = LoggerFactory.getLogger(JobReferralServiceImpl.class);
    private static final String DEFAULT_REFERRAL_STATUS = "SUBMITTED";

    private final ReferralRepository referralRepository;
    private final JobOpeningRepository jobOpeningRepository;
    private final EmployeeRepository employeeRepository;
    private final ReferralStatusRepository referralStatusRepository;
    private final FileStorageService fileStorageService;
    private final ModelMapper modelMapper;

    public JobReferralServiceImpl(
            ReferralRepository referralRepository,
            JobOpeningRepository jobOpeningRepository,
            EmployeeRepository employeeRepository,
            ReferralStatusRepository referralStatusRepository,
            FileStorageService fileStorageService,
            ModelMapper modelMapper) {
        this.referralRepository = referralRepository;
        this.jobOpeningRepository = jobOpeningRepository;
        this.employeeRepository = employeeRepository;
        this.referralStatusRepository = referralStatusRepository;
        this.fileStorageService = fileStorageService;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public JobReferralDto createReferral(CreateReferralRequest referralRequest, Long referrerId) {
        logger.info("Creating referral for job ID: {} by referrer ID: {}", referralRequest.getJobId(), referrerId);

        try {
            JobOpenings jobOpening = jobOpeningRepository.findById(referralRequest.getJobId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Job opening not found with ID: " + referralRequest.getJobId()));

            Employees referrer = employeeRepository.findById(referrerId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Referrer not found with ID: " + referrerId));

            ReferralStatus initialStatus = referralStatusRepository.findByStatusName(DEFAULT_REFERRAL_STATUS)
                    .orElseGet(() -> referralStatusRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("No referral statuses configured in the system")));

            Referrals referral = new Referrals();
            referral.setJobOpening(jobOpening);
            referral.setReferrer(referrer);
            referral.setFriendEmail(referralRequest.getFriendEmail());
            referral.setFriendName(referralRequest.getFriendName());
            referral.setNote(referralRequest.getNote());
            referral.setReferralStatus(initialStatus);
            referral.setCreatedAt(LocalDateTime.now());
            referral.setUpdatedAt(LocalDateTime.now());
            referral.setUpdatedByEmployee(referrer);

            Referrals savedReferral = referralRepository.save(referral);
            logger.info("Successfully created referral with ID: {}", savedReferral.getReferralId());

            if (referralRequest.getCvFile() != null && !referralRequest.getCvFile().isEmpty()) {
                String cvFilePath = fileStorageService.storeReferralCV(referralRequest.getCvFile(),
                        savedReferral.getReferralId());
                savedReferral.setCvFilePath(cvFilePath);
                savedReferral = referralRepository.save(savedReferral);
                logger.info("Successfully stored CV file for referral ID: {}", savedReferral.getReferralId());
            }

            // TODO: Send email to HR owner of job, also along with referral details and CV attachment

            return mapToDto(savedReferral);

        } catch (EntityNotFoundException e) {
            logger.error("Error creating referral: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error creating referral: {}", e.getMessage());
            throw new RuntimeException("Database error while creating referral", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public JobReferralDto getReferralById(Long referralId) {
        logger.info("Fetching referral with ID: {}", referralId);

        try {
            Referrals referral = referralRepository.findById(referralId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Referral not found with ID: " + referralId));
            return mapToDto(referral);

        } catch (EntityNotFoundException e) {
            logger.error("Error fetching referral: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error fetching referral: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching referral", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobReferralDto> getMyReferrals(Long employeeId) {
        logger.info("Fetching referrals for employee ID: {}", employeeId);

        try {
            List<Referrals> referrals = referralRepository.findByReferrer_EmployeeId(employeeId);
            List<JobReferralDto> result = referrals.stream()
                    .map(this::mapToDto)
                    .toList();

            logger.debug("Found {} referrals for employee ID: {}", result.size(), employeeId);
            return result;

        } catch (DataAccessException e) {
            logger.error("Database error fetching referrals for employee: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching employee referrals", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobReferralDto> getAllReferrals() {
        logger.info("Fetching all referrals");

        try {
            List<Referrals> referrals = referralRepository.findAll();
            List<JobReferralDto> result = referrals.stream()
                    .map(this::mapToDto)
                    .toList();

            logger.debug("Found {} total referrals", result.size());
            return result;

        } catch (DataAccessException e) {
            logger.error("Database error fetching all referrals: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching all referrals", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobReferralDto> getReferralsByJob(Long jobId) {
        logger.info("Fetching referrals for job ID: {}", jobId);

        try {
            List<Referrals> referrals = referralRepository.findByJobOpening_JobId(jobId);
            List<JobReferralDto> result = referrals.stream()
                    .map(this::mapToDto)
                    .toList();

            logger.debug("Found {} referrals for job ID: {}", result.size(), jobId);
            return result;

        } catch (DataAccessException e) {
            logger.error("Database error fetching referrals for job: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching job referrals", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobReferralDto> getReferralsByStatus(Long statusId) {
        logger.info("Fetching referrals with status ID: {}", statusId);

        try {
            List<Referrals> referrals = referralRepository.findByReferralStatus_StatusId(statusId);
            List<JobReferralDto> result = referrals.stream()
                    .map(this::mapToDto)
                    .toList();

            logger.debug("Found {} referrals with status ID: {}", result.size(), statusId);
            return result;

        } catch (DataAccessException e) {
            logger.error("Database error fetching referrals by status: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching referrals by status", e);
        }
    }

    @Override
    @Transactional
    public JobReferralDto updateReferralStatus(Long referralId, UpdateReferralStatusRequest statusRequest, Long hrId) {
        logger.info("Updating referral status for referral ID: {} to status ID: {}", referralId,
                statusRequest.getStatusId());

        try {
            Referrals referral = referralRepository.findById(referralId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Referral not found with ID: " + referralId));

            ReferralStatus newStatus = referralStatusRepository.findById(statusRequest.getStatusId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Referral status not found with ID: " + statusRequest.getStatusId()));

            Employees updatingEmployee = employeeRepository.findById(hrId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Updating employee not found with ID: " + hrId));

            referral.setReferralStatus(newStatus);
            referral.setUpdatedAt(LocalDateTime.now());
            referral.setUpdatedByEmployee(updatingEmployee);

            Referrals updatedReferral = referralRepository.save(referral);
            logger.info("Successfully updated referral status for referral ID: {}", referralId);

            return mapToDto(updatedReferral);

        } catch (EntityNotFoundException e) {
            logger.error("Error updating referral status: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error updating referral status: {}", e.getMessage());
            throw new RuntimeException("Database error while updating referral status", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReferralStatusDto> getAllReferralStatuses() {
        logger.info("Fetching all referral statuses");

        try {
            List<ReferralStatusDto> statuses = referralStatusRepository.findAll()
                    .stream()
                    .map(this::mapStatusToDto)
                    .toList();

            logger.debug("Found {} referral statuses", statuses.size());
            return statuses;

        } catch (DataAccessException e) {
            logger.error("Database error fetching referral statuses: {}", e.getMessage());
            throw new RuntimeException("Database error while fetching referral statuses", e);
        }
    }

    @Override
    @Transactional
    public void deleteReferral(Long referralId) {
        logger.info("Deleting referral with ID: {}", referralId);

        try {
            Referrals referral = referralRepository.findById(referralId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Referral not found with ID: " + referralId));

            if (referral.getCvFilePath() != null) {
                fileStorageService.deleteFile(referral.getCvFilePath());
                logger.info("Deleted CV file for referral ID: {}", referralId);
            }

            referralRepository.delete(referral);
            logger.info("Successfully deleted referral with ID: {}", referralId);

        } catch (EntityNotFoundException e) {
            logger.error("Error deleting referral: {}", e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            logger.error("Database error deleting referral: {}", e.getMessage());
            throw new RuntimeException("Database error while deleting referral", e);
        }
    }

    private JobReferralDto mapToDto(Referrals referral) {
        JobReferralDto dto = modelMapper.map(referral, JobReferralDto.class);

        JobOpeningDto jobOpeningDto = modelMapper.map(referral.getJobOpening(), JobOpeningDto.class);
        jobOpeningDto.setJobHROwnerId(referral.getJobOpening().getJobHROwner().getEmployeeId());
        dto.setJobOpening(jobOpeningDto);

        EmployeeSummaryDto referrerDto = modelMapper.map(referral.getReferrer(), EmployeeSummaryDto.class);
        dto.setReferrer(referrerDto);

        ReferralStatusDto statusDto = mapStatusToDto(referral.getReferralStatus());
        dto.setReferralStatus(statusDto);

        if (referral.getUpdatedByEmployee() != null) {
            EmployeeSummaryDto updatedByDto = modelMapper.map(
                    referral.getUpdatedByEmployee(),
                    EmployeeSummaryDto.class);
            dto.setUpdatedByEmployee(updatedByDto);
        }

        return dto;
    }

    private ReferralStatusDto mapStatusToDto(ReferralStatus status) {
        ReferralStatusDto dto = modelMapper.map(status, ReferralStatusDto.class);

        if (status.getUpdatedByEmployee() != null) {
            EmployeeSummaryDto updatedByDto = modelMapper.map(
                    status.getUpdatedByEmployee(),
                    EmployeeSummaryDto.class);
            dto.setUpdatedByEmployee(updatedByDto);
        }

        return dto;
    }
}
