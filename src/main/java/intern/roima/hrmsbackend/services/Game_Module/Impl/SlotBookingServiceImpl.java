package intern.roima.hrmsbackend.services.Game_Module.Impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intern.roima.hrmsbackend.dtos.Requests.AddParticipantRequest;
import intern.roima.hrmsbackend.dtos.Requests.BookSlotRequest;
import intern.roima.hrmsbackend.dtos.Responses.EmployeeSummaryDto;
import intern.roima.hrmsbackend.dtos.Responses.GameSlotDto;
import intern.roima.hrmsbackend.dtos.Responses.SlotBookingDto;
import intern.roima.hrmsbackend.dtos.Responses.SlotParticipantDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.entities.Game_Module.BookingStatus;
import intern.roima.hrmsbackend.entities.Game_Module.GameSlots;
import intern.roima.hrmsbackend.entities.Game_Module.SlotBookings;
import intern.roima.hrmsbackend.entities.Game_Module.SlotParticipants;
import intern.roima.hrmsbackend.entities.Game_Module.SlotStatus;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.repositories.Game_Module.BookingStatusRepository;
import intern.roima.hrmsbackend.repositories.Game_Module.GameSlotsRepository;
import intern.roima.hrmsbackend.repositories.Game_Module.SlotBookingsRepository;
import intern.roima.hrmsbackend.repositories.Game_Module.SlotParticipantsRepository;
import intern.roima.hrmsbackend.repositories.Game_Module.SlotStatusRepository;
import intern.roima.hrmsbackend.services.Game_Module.SlotBookingService;
import intern.roima.hrmsbackend.services.Message_Module.NotificationService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class SlotBookingServiceImpl implements SlotBookingService {

    private static final Logger logger = LoggerFactory.getLogger(SlotBookingServiceImpl.class);

    private static final String SLOT_STATUS_AVAILABLE = "AVAILABLE";
    private static final String SLOT_STATUS_BOOKED = "BOOKED";
    private static final String SLOT_STATUS_FULL = "FULL";
    private static final String BOOKING_STATUS_ACTIVE = "ACTIVE";
    private static final String BOOKING_STATUS_CANCELLED = "CANCELLED";

    private final SlotBookingsRepository slotBookingsRepository;
    private final GameSlotsRepository gameSlotsRepository;
    private final EmployeeRepository employeeRepository;
    private final BookingStatusRepository bookingStatusRepository;
    private final SlotStatusRepository slotStatusRepository;
    private final SlotParticipantsRepository slotParticipantsRepository;
    private final NotificationService notificationService;

    public SlotBookingServiceImpl(
            SlotBookingsRepository slotBookingsRepository,
            GameSlotsRepository gameSlotsRepository,
            EmployeeRepository employeeRepository,
            BookingStatusRepository bookingStatusRepository,
            SlotStatusRepository slotStatusRepository,
            SlotParticipantsRepository slotParticipantsRepository,
            NotificationService notificationService) {
        this.slotBookingsRepository = slotBookingsRepository;
        this.gameSlotsRepository = gameSlotsRepository;
        this.employeeRepository = employeeRepository;
        this.bookingStatusRepository = bookingStatusRepository;
        this.slotStatusRepository = slotStatusRepository;
        this.slotParticipantsRepository = slotParticipantsRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public SlotBookingDto bookSlot(BookSlotRequest request, Long employeeId) {
        logger.info("Employee {} is booking slot {}", employeeId, request.getSlotId());
        GameSlots slot = gameSlotsRepository.findById(request.getSlotId())
                .orElseThrow(() -> new EntityNotFoundException("Slot not found with ID: " + request.getSlotId()));

        Employees employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        if (!employee.isActive()) {
            throw new IllegalStateException("Employee account is inactive");
        }

        if (!SLOT_STATUS_AVAILABLE.equals(slot.getStatus().getStatusName())) {
            throw new IllegalStateException("Slot is not available for booking. Only AVAILABLE slots can be booked");
        }

        if (hasEmployeeBookedSlotOnDate(employeeId, slot.getSlotDate())) {
            throw new IllegalStateException("You already have a booking on this date");
        }

        if (isEmployeeParticipantOnDate(employeeId, slot.getSlotDate())) {
            throw new IllegalStateException("You are already a participant in another booking on this date");
        }

        List<SlotBookings> activeBookings = slotBookingsRepository.findActiveBookingsBySlot(request.getSlotId());
        if (!activeBookings.isEmpty()) {
            throw new IllegalStateException("Slot is already booked by another employee");
        }

        BookingStatus activeStatus = bookingStatusRepository.findByStatusName(BOOKING_STATUS_ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("Booking status 'ACTIVE' not found"));

        SlotBookings booking = new SlotBookings();
        booking.setSlot(slot);
        booking.setBookedBy(employee);
        booking.setBookingStatus(activeStatus);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        booking.setUpdatedByEmployee(employee);

        SlotBookings savedBooking = slotBookingsRepository.save(booking);
        logger.info("Successfully created booking with ID: {}", savedBooking.getBookingId());

        String slotDateTime = formatSlotDateTime(slot.getStartDateTime(), slot.getEndDateTime());
        notificationService.sendSlotBookingNotification(
                employeeId,
                slot.getSlotId(),
                slot.getGame().getGameName(),
                slotDateTime);
        logger.info("Sent booking notification to employee {}", employeeId);

        List<Long> participantIdsForNotification = new ArrayList<>();

        if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
            for (Long participantId : request.getParticipantIds()) {
                if (participantId.equals(employeeId)) {
                    logger.info("Skipping booking owner {} from participants list", participantId);
                    continue;
                }

                Employees participant = employeeRepository.findById(participantId)
                        .orElseThrow(
                                () -> new EntityNotFoundException("Participant not found with ID: " + participantId));

                if (!participant.isActive()) {
                    throw new IllegalStateException("Participant account is inactive");
                }

                if (hasEmployeeBookedSlotOnDate(participantId, slot.getSlotDate())) {
                    throw new IllegalStateException("Participant already has a booking on this date");
                }

                if (isEmployeeParticipantOnDate(participantId, slot.getSlotDate())) {
                    throw new IllegalStateException(
                            "Participant is already participating in another booking on this date");
                }

                Long totalParticipants = slotParticipantsRepository.countActiveParticipantsBySlot(request.getSlotId());
                if (totalParticipants + 1 >= slot.getMaxPlayers()) {
                    throw new IllegalStateException("Slot is full and cannot accept more participants");
                }

                SlotParticipants slotParticipant = new SlotParticipants();
                slotParticipant.setBooking(savedBooking);
                slotParticipant.setEmployee(participant);
                slotParticipant.setCreatedAt(LocalDateTime.now());
                slotParticipant.setUpdatedAt(LocalDateTime.now());
                slotParticipant.setUpdatedByEmployee(employee);

                slotParticipantsRepository.save(slotParticipant);
                logger.info("Added participant {} to booking {}", participantId, savedBooking.getBookingId());
                participantIdsForNotification.add(participantId);
            }
        }

        if (!participantIdsForNotification.isEmpty()) {
            notificationService.sendSlotBookingNotifications(
                    participantIdsForNotification,
                    slot.getSlotId(),
                    slot.getGame().getGameName(),
                    slotDateTime);
            logger.info("Sent booking notifications to {} participants", participantIdsForNotification.size());
        }

        updateSlotStatus(slot);
        logger.info("Successfully booked slot {} for employee {}", request.getSlotId(), employeeId);

        return getBookingById(savedBooking.getBookingId());
    }

    @Override
    @Transactional(readOnly = true)
    public SlotBookingDto getBookingById(Long bookingId) {
        logger.info("Fetching booking with ID: {}", bookingId);

        SlotBookings booking = slotBookingsRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId));

        return mapToSlotBookingDto(booking);
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, Long employeeId) {
        logger.info("Employee {} is cancelling booking {}", employeeId, bookingId);

        SlotBookings booking = slotBookingsRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId));

        Employees employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        if (!booking.getBookedBy().getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("You can only cancel your own bookings");
        }

        if (!booking.getBookingStatus().getStatusName().equals(BOOKING_STATUS_ACTIVE)) {
            throw new IllegalStateException("Only active bookings can be cancelled");
        }

        BookingStatus cancelledStatus = bookingStatusRepository.findByStatusName(BOOKING_STATUS_CANCELLED)
                .orElseThrow(() -> new EntityNotFoundException("Booking status 'CANCELLED' not found"));

        booking.setBookingStatus(cancelledStatus);
        booking.setUpdatedAt(LocalDateTime.now());
        booking.setUpdatedByEmployee(employee);

        slotBookingsRepository.save(booking);

        updateSlotStatus(booking.getSlot());

        logger.info("Successfully cancelled booking with ID: {}", bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotBookingDto> getMyBookings(Long employeeId) {
        logger.info("Fetching all bookings for employee: {}", employeeId);

        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        List<SlotBookings> bookings = slotBookingsRepository.findByBookedBy_EmployeeId(employeeId);
        return bookings.stream()
                .map(this::mapToSlotBookingDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotBookingDto> getMyActiveBookings(Long employeeId) {
        logger.info("Fetching active bookings for employee: {}", employeeId);

        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        List<SlotBookings> bookings = slotBookingsRepository
                .findByBookedBy_EmployeeIdAndBookingStatus_StatusName(employeeId, BOOKING_STATUS_ACTIVE);
        return bookings.stream()
                .map(this::mapToSlotBookingDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotBookingDto> getMyBookingHistory(Long employeeId) {
        logger.info("Fetching booking history for employee: {}", employeeId);

        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        List<SlotBookings> bookings = slotBookingsRepository.findByBookedBy_EmployeeId(employeeId);
        return bookings.stream()
                .filter(booking -> !booking.getBookingStatus().getStatusName().equals(BOOKING_STATUS_ACTIVE))
                .map(this::mapToSlotBookingDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotBookingDto> getBookingsForSlot(Long slotId) {
        logger.info("Fetching all bookings for slot: {}", slotId);

        gameSlotsRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Slot not found with ID: " + slotId));

        List<SlotBookings> bookings = slotBookingsRepository.findBySlot_SlotId(slotId);
        return bookings.stream()
                .map(this::mapToSlotBookingDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSlotDto> getAllAvailableSlots() {
        logger.info("Fetching all available slots");

        List<GameSlots> slots = gameSlotsRepository.findAll();
        return slots.stream()
                .filter(slot -> Arrays.asList(SLOT_STATUS_AVAILABLE, SLOT_STATUS_BOOKED)
                        .contains(slot.getStatus().getStatusName()))
                .map(this::mapToGameSlotDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSlotDto> getAvailableSlotsByGame(Long gameId) {
        logger.info("Fetching available slots for game: {}", gameId);

        List<GameSlots> slots = gameSlotsRepository.findByGame_GameId(gameId);
        return slots.stream()
                .filter(slot -> Arrays.asList(SLOT_STATUS_AVAILABLE, SLOT_STATUS_BOOKED)
                        .contains(slot.getStatus().getStatusName()))
                .map(this::mapToGameSlotDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSlotDto> getAvailableSlotsByDate(LocalDate date) {
        logger.info("Fetching available slots for date: {}", date);

        List<GameSlots> slots = gameSlotsRepository.findByDate(date);
        return slots.stream()
                .filter(slot -> Arrays.asList(SLOT_STATUS_AVAILABLE, SLOT_STATUS_BOOKED)
                        .contains(slot.getStatus().getStatusName()))
                .map(this::mapToGameSlotDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSlotDto> getAvailableSlotsByGameAndDate(Long gameId, LocalDate date) {
        logger.info("Fetching available slots for game: {} on date: {}", gameId, date);

        List<GameSlots> slots = gameSlotsRepository.findByGameAndDate(gameId, date);
        return slots.stream()
                .filter(slot -> Arrays.asList(SLOT_STATUS_AVAILABLE, SLOT_STATUS_BOOKED)
                        .contains(slot.getStatus().getStatusName()))
                .map(this::mapToGameSlotDto)
                .toList();
    }

    @Override
    @Transactional
    public SlotParticipantDto addParticipant(Long bookingId, AddParticipantRequest request, Long addedBy) {
        logger.info("Employee {} is adding participant {} to booking {}", addedBy, request.getParticipantEmployeeId(),
                bookingId);

        SlotBookings booking = slotBookingsRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId));

        Employees addingEmployee = employeeRepository.findById(addedBy)
                .orElseThrow(() -> new EntityNotFoundException("Adding employee not found with ID: " + addedBy));

        if (!booking.getBookedBy().getEmployeeId().equals(addedBy)) {
            throw new IllegalStateException("Only the booking owner can add participants");
        }

        if (!booking.getBookingStatus().getStatusName().equals(BOOKING_STATUS_ACTIVE)) {
            throw new IllegalStateException("Can only add participants to active bookings");
        }

        Employees participant = employeeRepository.findById(request.getParticipantEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Participant not found with ID: " + request.getParticipantEmployeeId()));

        if (!participant.isActive()) {
            throw new IllegalStateException("Participant account is inactive");
        }

        if (hasEmployeeBookedSlotOnDate(request.getParticipantEmployeeId(), booking.getSlot().getSlotDate())) {
            throw new IllegalStateException("Participant already has a booking on this date");
        }

        if (isEmployeeParticipantOnDate(request.getParticipantEmployeeId(), booking.getSlot().getSlotDate())) {
            throw new IllegalStateException("Participant is already participating in another booking on this date");
        }

        List<SlotParticipants> existingParticipants = slotParticipantsRepository.findByBookingAndEmployee(
                bookingId, request.getParticipantEmployeeId());
        if (!existingParticipants.isEmpty()) {
            throw new IllegalStateException("Participant is already added to this booking");
        }

        Long currentParticipants = slotParticipantsRepository
                .countActiveParticipantsBySlot(booking.getSlot().getSlotId());
        // Include the booking owner in the participant count
        if (currentParticipants + 1 >= booking.getSlot().getMaxPlayers()) {
            throw new IllegalStateException("Slot is full and cannot accept more participants");
        }

        SlotParticipants slotParticipant = new SlotParticipants();
        slotParticipant.setBooking(booking);
        slotParticipant.setEmployee(participant);
        slotParticipant.setCreatedAt(LocalDateTime.now());
        slotParticipant.setUpdatedAt(LocalDateTime.now());
        slotParticipant.setUpdatedByEmployee(addingEmployee);

        SlotParticipants savedParticipant = slotParticipantsRepository.save(slotParticipant);
        logger.info("Successfully added participant {} to booking {}", request.getParticipantEmployeeId(), bookingId);

        String slotDateTime = formatSlotDateTime(booking.getSlot().getStartDateTime(),
                booking.getSlot().getEndDateTime());
        notificationService.sendSlotBookingNotification(
                request.getParticipantEmployeeId(),
                booking.getSlot().getSlotId(),
                booking.getSlot().getGame().getGameName(),
                slotDateTime);
        logger.info("Sent booking notification to participant {}", request.getParticipantEmployeeId());

        updateSlotStatus(booking.getSlot());

        return mapToSlotParticipantDto(savedParticipant);
    }

    @Override
    @Transactional
    public void removeParticipant(Long participantId, Long employeeId) {
        logger.info("Employee {} is removing participant {}", employeeId, participantId);

        SlotParticipants participant = slotParticipantsRepository.findById(participantId)
                .orElseThrow(() -> new EntityNotFoundException("Participant not found with ID: " + participantId));

        SlotBookings booking = participant.getBooking();

        if (!booking.getBookedBy().getEmployeeId().equals(employeeId)
                && !participant.getEmployee().getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("You can only remove yourself or participants from your own bookings");
        }

        if (!booking.getBookingStatus().getStatusName().equals(BOOKING_STATUS_ACTIVE)) {
            throw new IllegalStateException("Can only remove participants from active bookings");
        }

        slotParticipantsRepository.delete(participant);
        logger.info("Successfully removed participant with ID: {}", participantId);

        updateSlotStatus(booking.getSlot());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotParticipantDto> getParticipantsForBooking(Long bookingId) {
        logger.info("Fetching participants for booking: {}", bookingId);

        slotBookingsRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with ID: " + bookingId));

        List<SlotParticipants> participants = slotParticipantsRepository.findByBooking_BookingId(bookingId);
        return participants.stream()
                .map(this::mapToSlotParticipantDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingStatus> getAllBookingStatuses() {
        logger.info("Fetching all booking statuses");
        return bookingStatusRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEmployeeBookedSlotOnDate(Long employeeId, LocalDate date) {
        Long bookingCount = slotBookingsRepository.countBookingsByEmployeeAndDate(employeeId, date);
        return bookingCount > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmployeeParticipantOnDate(Long employeeId, LocalDate date) {
        List<SlotParticipants> participations = slotParticipantsRepository
                .findActiveParticipationsByEmployeeAndDate(employeeId, date);
        return !participations.isEmpty();
    }

    private void updateSlotStatus(GameSlots slot) {
        Long currentParticipants = slotParticipantsRepository.countActiveParticipantsBySlot(slot.getSlotId());

        SlotStatus newStatus;
        if (currentParticipants >= slot.getMaxPlayers()) {
            newStatus = slotStatusRepository.findByStatusName(SLOT_STATUS_FULL)
                    .orElseThrow(() -> new EntityNotFoundException("Slot status 'FULL' not found"));
        } else if (currentParticipants > 0) {
            newStatus = slotStatusRepository.findByStatusName(SLOT_STATUS_BOOKED)
                    .orElseThrow(() -> new EntityNotFoundException("Slot status 'BOOKED' not found"));
        } else {
            newStatus = slotStatusRepository.findByStatusName(SLOT_STATUS_AVAILABLE)
                    .orElseThrow(() -> new EntityNotFoundException("Slot status 'AVAILABLE' not found"));
        }

        if (!slot.getStatus().getSlotStatusId().equals(newStatus.getSlotStatusId())) {
            slot.setStatus(newStatus);
            slot.setUpdatedAt(LocalDateTime.now());
            gameSlotsRepository.save(slot);
            logger.info("Updated slot {} status to {}", slot.getSlotId(), newStatus.getStatusName());
        }
    }

    private SlotBookingDto mapToSlotBookingDto(SlotBookings booking) {
        SlotBookingDto dto = new SlotBookingDto();
        dto.setBookingId(booking.getBookingId());
        dto.setSlotId(booking.getSlot().getSlotId());
        dto.setSlotDate(booking.getSlot().getSlotDate());
        dto.setSlotStartDateTime(booking.getSlot().getStartDateTime());
        dto.setSlotEndDateTime(booking.getSlot().getEndDateTime());
        dto.setGameId(booking.getSlot().getGame().getGameId());
        dto.setGameName(booking.getSlot().getGame().getGameName());
        dto.setSlotMaxPlayers(booking.getSlot().getMaxPlayers());
        dto.setBookedByEmployeeId(booking.getBookedBy().getEmployeeId());
        dto.setBookedByEmployeeName(booking.getBookedBy().getFirstName() + " " + booking.getBookedBy().getLastName());
        dto.setBookingStatusId(booking.getBookingStatus().getBookingStatusId());
        dto.setBookingStatusName(booking.getBookingStatus().getStatusName());

        List<SlotParticipants> participants = slotParticipantsRepository
                .findByBooking_BookingId(booking.getBookingId());
        dto.setParticipantCount(participants.size());

        List<EmployeeSummaryDto> participantDtos = new ArrayList<>();
        for (SlotParticipants participant : participants) {
            participantDtos.add(mapToEmployeeSummaryDto(participant.getEmployee()));
        }
        dto.setParticipants(participantDtos);

        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        if (booking.getUpdatedByEmployee() != null) {
            dto.setUpdatedByEmployeeId(booking.getUpdatedByEmployee().getEmployeeId());
        }
        return dto;
    }

    private GameSlotDto mapToGameSlotDto(GameSlots slot) {
        GameSlotDto dto = new GameSlotDto();
        dto.setSlotId(slot.getSlotId());
        dto.setGameId(slot.getGame().getGameId());
        dto.setGameName(slot.getGame().getGameName());
        dto.setSlotDate(slot.getSlotDate());
        dto.setStartDateTime(slot.getStartDateTime());
        dto.setEndDateTime(slot.getEndDateTime());
        dto.setMaxPlayers(slot.getMaxPlayers());

        Long currentParticipants = slotParticipantsRepository.countActiveParticipantsBySlot(slot.getSlotId());
        dto.setCurrentParticipants(currentParticipants.intValue());
        dto.setAvailableSpots(slot.getMaxPlayers() - currentParticipants.intValue());

        dto.setSlotStatusId(slot.getStatus().getSlotStatusId());
        dto.setSlotStatusName(slot.getStatus().getStatusName());
        dto.setCreatedAt(slot.getCreatedAt());
        dto.setUpdatedAt(slot.getUpdatedAt());
        if (slot.getUpdatedByEmployee() != null) {
            dto.setUpdatedByEmployeeId(slot.getUpdatedByEmployee().getEmployeeId());
        }
        return dto;
    }

    private SlotParticipantDto mapToSlotParticipantDto(SlotParticipants participant) {
        SlotParticipantDto dto = new SlotParticipantDto();
        dto.setSlotParticipantId(participant.getSlotParticipantId());
        dto.setBookingId(participant.getBooking().getBookingId());
        dto.setEmployee(mapToEmployeeSummaryDto(participant.getEmployee()));
        dto.setCreatedAt(participant.getCreatedAt());
        return dto;
    }

    private EmployeeSummaryDto mapToEmployeeSummaryDto(Employees employee) {
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

    private String formatSlotDateTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy");
        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

        String date = startDateTime.format(dateFormatter);
        String startTime = startDateTime.format(timeFormatter);
        String endTime = endDateTime.format(timeFormatter);

        return date + " from " + startTime + " to " + endTime;
    }
}
