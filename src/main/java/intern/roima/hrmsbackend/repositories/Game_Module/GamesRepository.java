package intern.roima.hrmsbackend.repositories.Game_Module;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import intern.roima.hrmsbackend.entities.Game_Module.Games;

public interface GamesRepository extends JpaRepository<Games, Long> {
    Optional<Games> findByGameName(String gameName);
}
