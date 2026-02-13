package intern.roima.hrmsbackend.repositories.Game_Module;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Game_Module.GameConfigurations;

public interface GameConfigurationsRepository extends JpaRepository<GameConfigurations, Long> {
    Optional<GameConfigurations> findByGame_GameId(Long gameId);
    List<GameConfigurations> findAllByGame_GameId(Long gameId);
}
