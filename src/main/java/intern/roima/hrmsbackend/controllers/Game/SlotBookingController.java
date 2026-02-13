package intern.roima.hrmsbackend.controllers.Game;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import intern.roima.hrmsbackend.dtos.Requests.AddParticipantRequest;
import intern.roima.hrmsbackend.dtos.Requests.BookSlotRequest;
import intern.roima.hrmsbackend.dtos.Responses.GameSlotDto;
import intern.roima.hrmsbackend.dtos.Responses.SlotBookingDto;
import intern.roima.hrmsbackend.dtos.Responses.SlotParticipantDto;
import intern.roima.hrmsbackend.entities.Game_Module.BookingStatus;
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Game_Module.SlotBookingService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class SlotBookingController {

    private final SlotBookingService slotBookingService;

    public SlotBookingController(SlotBookingService slotBookingService) {
        this.slotBookingService = slotBookingService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<SlotBookingDto> bookSlot(
            @Valid @RequestBody BookSlotRequest request,
            @CurrentUser Long employeeId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(slotBookingService.bookSlot(request, employeeId));
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<SlotBookingDto> getBookingById(@PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok(slotBookingService.getBookingById(bookingId));
    }

    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable("bookingId") Long bookingId,
            @CurrentUser Long employeeId) {
        slotBookingService.cancelBooking(bookingId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<SlotBookingDto>> getMyBookings(@CurrentUser Long employeeId) {
        return ResponseEntity.ok(slotBookingService.getMyBookings(employeeId));
    }

    @GetMapping("/my-bookings/active")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<SlotBookingDto>> getMyActiveBookings(@CurrentUser Long employeeId) {
        return ResponseEntity.ok(slotBookingService.getMyActiveBookings(employeeId));
    }

    @GetMapping("/my-bookings/history")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<SlotBookingDto>> getMyBookingHistory(@CurrentUser Long employeeId) {
        return ResponseEntity.ok(slotBookingService.getMyBookingHistory(employeeId));
    }

    @GetMapping("/slot/{slotId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<SlotBookingDto>> getBookingsForSlot(@PathVariable("slotId") Long slotId) {
        return ResponseEntity.ok(slotBookingService.getBookingsForSlot(slotId));
    }

    @GetMapping("/available-slots")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<GameSlotDto>> getAllAvailableSlots() {
        return ResponseEntity.ok(slotBookingService.getAllAvailableSlots());
    }

    @GetMapping("/available-slots/game/{gameId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<GameSlotDto>> getAvailableSlotsByGame(@PathVariable("gameId") Long gameId) {
        return ResponseEntity.ok(slotBookingService.getAvailableSlotsByGame(gameId));
    }

    @GetMapping("/available-slots/date")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<GameSlotDto>> getAvailableSlotsByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotBookingService.getAvailableSlotsByDate(date));
    }

    @GetMapping("/available-slots/game/{gameId}/date")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<GameSlotDto>> getAvailableSlotsByGameAndDate(
            @PathVariable("gameId") Long gameId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotBookingService.getAvailableSlotsByGameAndDate(gameId, date));
    }

    @PostMapping("/{bookingId}/participants")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<SlotParticipantDto> addParticipant(
            @PathVariable("bookingId") Long bookingId,
            @Valid @RequestBody AddParticipantRequest request,
            @CurrentUser Long addedBy) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(slotBookingService.addParticipant(bookingId, request, addedBy));
    }

    @DeleteMapping("/participants/{participantId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Void> removeParticipant(
            @PathVariable("participantId") Long participantId,
            @CurrentUser Long employeeId) {
        slotBookingService.removeParticipant(participantId, employeeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookingId}/participants")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<SlotParticipantDto>> getParticipantsForBooking(
            @PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok(slotBookingService.getParticipantsForBooking(bookingId));
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<BookingStatus>> getAllBookingStatuses() {
        return ResponseEntity.ok(slotBookingService.getAllBookingStatuses());
    }

    @GetMapping("/check-booking")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Boolean> hasEmployeeBookedSlotOnDate(
            @CurrentUser Long employeeId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotBookingService.hasEmployeeBookedSlotOnDate(employeeId, date));
    }

    @GetMapping("/check-participant")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Boolean> isEmployeeParticipantOnDate(
            @CurrentUser Long employeeId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotBookingService.isEmployeeParticipantOnDate(employeeId, date));
    }
}
