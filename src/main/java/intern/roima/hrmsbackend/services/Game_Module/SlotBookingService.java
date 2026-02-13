package intern.roima.hrmsbackend.services.Game_Module;

import java.time.LocalDate;
import java.util.List;

import intern.roima.hrmsbackend.dtos.Requests.AddParticipantRequest;
import intern.roima.hrmsbackend.dtos.Requests.BookSlotRequest;
import intern.roima.hrmsbackend.dtos.Responses.GameSlotDto;
import intern.roima.hrmsbackend.dtos.Responses.SlotBookingDto;
import intern.roima.hrmsbackend.dtos.Responses.SlotParticipantDto;
import intern.roima.hrmsbackend.entities.Game_Module.BookingStatus;

public interface SlotBookingService {

    SlotBookingDto bookSlot(BookSlotRequest request, Long employeeId);

    SlotBookingDto getBookingById(Long bookingId);

    void cancelBooking(Long bookingId, Long employeeId);

    List<SlotBookingDto> getMyBookings(Long employeeId);

    List<SlotBookingDto> getMyActiveBookings(Long employeeId);

    List<SlotBookingDto> getMyBookingHistory(Long employeeId);

    List<SlotBookingDto> getBookingsForSlot(Long slotId);

    List<GameSlotDto> getAllAvailableSlots();

    List<GameSlotDto> getAvailableSlotsByGame(Long gameId);

    List<GameSlotDto> getAvailableSlotsByDate(LocalDate date);

    List<GameSlotDto> getAvailableSlotsByGameAndDate(Long gameId, LocalDate date);

    SlotParticipantDto addParticipant(Long bookingId, AddParticipantRequest request, Long addedBy);

    void removeParticipant(Long participantId, Long employeeId);

    List<SlotParticipantDto> getParticipantsForBooking(Long bookingId);

    List<BookingStatus> getAllBookingStatuses();

    boolean hasEmployeeBookedSlotOnDate(Long employeeId, LocalDate date);

    boolean isEmployeeParticipantOnDate(Long employeeId, LocalDate date);

}
