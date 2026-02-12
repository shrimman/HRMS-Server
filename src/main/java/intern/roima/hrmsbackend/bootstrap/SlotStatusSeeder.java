package intern.roima.hrmsbackend.bootstrap;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import intern.roima.hrmsbackend.entities.Game_Module.SlotStatus;
import intern.roima.hrmsbackend.repositories.Game_Module.SlotStatusRepository;

@Component
public class SlotStatusSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final SlotStatusRepository slotStatusRepository;

    public SlotStatusSeeder(SlotStatusRepository slotStatusRepository) {
        this.slotStatusRepository = slotStatusRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadSlotStatuses();
    }

    private void loadSlotStatuses() {
        String[][] statusData = {
                { "AVAILABLE", "Slot is available for booking" },
                { "BOOKED", "Slot has been booked" },
                { "FULL", "Slot is full, no more bookings allowed"},
                { "CANCELLED", "Slot has been cancelled" },
                { "COMPLETED", "Slot event has been completed" }
        };

        for (String[] status : statusData) {
            if (slotStatusRepository.findByStatusName(status[0]).isEmpty()) {
                SlotStatus slotStatus = new SlotStatus();
                slotStatus.setStatusName(status[0]);
                slotStatus.setDescription(status[1]);
                slotStatus.setActive(true);
                slotStatus.setCreatedAt(LocalDateTime.now());
                slotStatus.setUpdatedAt(LocalDateTime.now());
                slotStatusRepository.save(slotStatus);
            }
        }
    }
}
