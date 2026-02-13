package intern.roima.hrmsbackend.repositories.Game_Module;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import intern.roima.hrmsbackend.entities.Game_Module.SlotBookings;

public interface SlotBookingsRepository extends JpaRepository<SlotBookings, Long> {
    List<SlotBookings> findBySlot_SlotId(Long slotId);

    List<SlotBookings> findByBookedBy_EmployeeId(Long employeeId);

    List<SlotBookings> findByBookedBy_EmployeeIdAndBookingStatus_StatusName(Long employeeId, String statusName);

    @Query("SELECT sb FROM SlotBookings sb WHERE sb.bookedBy.employeeId = :employeeId AND sb.slot.slotDate = :date AND sb.bookingStatus.statusName = 'ACTIVE'")
    List<SlotBookings> findActiveBookingsByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    @Query("SELECT sb FROM SlotBookings sb WHERE sb.slot.slotId = :slotId AND sb.bookingStatus.statusName = 'ACTIVE'")
    List<SlotBookings> findActiveBookingsBySlot(@Param("slotId") Long slotId);

    @Query("SELECT COUNT(sb) FROM SlotBookings sb WHERE sb.bookedBy.employeeId = :employeeId AND sb.slot.slotDate = :date AND sb.bookingStatus.statusName IN ('ACTIVE', 'COMPLETED')")
    Long countBookingsByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);
}
