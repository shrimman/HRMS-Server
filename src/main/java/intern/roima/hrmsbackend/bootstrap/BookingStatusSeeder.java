package intern.roima.hrmsbackend.bootstrap;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import intern.roima.hrmsbackend.entities.Game_Module.BookingStatus;
import intern.roima.hrmsbackend.repositories.Game_Module.BookingStatusRepository;

@Component
public class BookingStatusSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final BookingStatusRepository bookingStatusRepository;

    public BookingStatusSeeder(BookingStatusRepository bookingStatusRepository) {
        this.bookingStatusRepository = bookingStatusRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadBookingStatuses();
    }

    private void loadBookingStatuses() {
        String[][] statusData = {
                {"ACTIVE", "Booking is active and confirmed"},
                {"CANCELLED", "Booking has been cancelled"},
                {"COMPLETED", "Booking event has been completed"},
        };

        for (String[] status : statusData) {
            if (bookingStatusRepository.findByStatusName(status[0]).isEmpty()) {
                BookingStatus bookingStatus = new BookingStatus();
                bookingStatus.setStatusName(status[0]);
                bookingStatus.setDescription(status[1]);
                bookingStatus.setActive(true);
                bookingStatus.setCreatedAt(LocalDateTime.now());
                bookingStatus.setUpdatedAt(LocalDateTime.now());
                bookingStatusRepository.save(bookingStatus);
            }
        }
    }
}
