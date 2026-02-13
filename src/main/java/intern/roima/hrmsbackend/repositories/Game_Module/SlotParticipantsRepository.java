package intern.roima.hrmsbackend.repositories.Game_Module;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import intern.roima.hrmsbackend.entities.Game_Module.SlotParticipants;

public interface SlotParticipantsRepository extends JpaRepository<SlotParticipants, Long> {
    List<SlotParticipants> findByBooking_BookingId(Long bookingId);

    List<SlotParticipants> findByEmployee_EmployeeId(Long employeeId);

    @Query("SELECT sp FROM SlotParticipants sp WHERE sp.employee.employeeId = :employeeId AND sp.booking.slot.slotDate = :date AND sp.booking.bookingStatus.statusName = 'ACTIVE'")
    List<SlotParticipants> findActiveParticipationsByEmployeeAndDate(@Param("employeeId") Long employeeId,
            @Param("date") LocalDate date);

    @Query("SELECT COUNT(DISTINCT sp.employee.employeeId) FROM SlotParticipants sp WHERE sp.booking.slot.slotId = :slotId AND sp.booking.bookingStatus.statusName = 'ACTIVE'")
    Long countActiveParticipantsBySlot(@Param("slotId") Long slotId);

    @Query("SELECT sp FROM SlotParticipants sp WHERE sp.booking.bookingId = :bookingId AND sp.employee.employeeId = :employeeId")
    List<SlotParticipants> findByBookingAndEmployee(@Param("bookingId") Long bookingId,
            @Param("employeeId") Long employeeId);

    void deleteByBooking_BookingId(Long bookingId);

    Integer countByBooking_Slot_SlotId(Long slotId);

    Integer countByBooking_BookingId(Long bookingId);

}
