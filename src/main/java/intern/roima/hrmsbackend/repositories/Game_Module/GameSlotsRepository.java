package intern.roima.hrmsbackend.repositories.Game_Module;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import intern.roima.hrmsbackend.entities.Game_Module.GameSlots;

public interface GameSlotsRepository extends JpaRepository<GameSlots, Long> {
        List<GameSlots> findByGame_GameId(Long gameId);

        List<GameSlots> findByStartDateTimeBetween(LocalDateTime start, LocalDateTime end);

        List<GameSlots> findByGame_GameIdAndStatus_StatusName(Long gameId, String statusName);

        List<GameSlots> findByStatus_StatusName(String statusName);

        @Query("SELECT gs FROM GameSlots gs WHERE gs.game.gameId = :gameId AND gs.slotDate = :slotsdate")
        List<GameSlots> findByGameAndDate(@Param("gameId") Long gameId, @Param("slotsdate") LocalDate slotsdate);

        @Query("SELECT gs FROM GameSlots gs WHERE gs.slotDate = :date")
        List<GameSlots> findByDate(@Param("date") LocalDate date);

        @Query("SELECT gs FROM GameSlots gs WHERE gs.game.gameId = :gameId AND gs.slotDate = :date AND gs.status.statusName = :statusName")
        List<GameSlots> findAvailableSlotsByGameAndDate(@Param("gameId") Long gameId, @Param("date") LocalDate date,
                        @Param("statusName") String statusName);

        List<GameSlots> findByGame_GameIdAndStartDateTimeBetween(Long gameId, LocalDateTime atStartOfDay,
                        LocalDateTime atTime);
}
