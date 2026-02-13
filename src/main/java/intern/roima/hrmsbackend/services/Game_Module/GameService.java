package intern.roima.hrmsbackend.services.Game_Module;

import java.time.LocalDate;
import java.util.List;

import intern.roima.hrmsbackend.dtos.Requests.CreateGameConfigRequest;
import intern.roima.hrmsbackend.dtos.Requests.CreateGameRequest;
import intern.roima.hrmsbackend.dtos.Requests.GenerateSlotsRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateGameConfigRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateGameRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateSlotRequest;
import intern.roima.hrmsbackend.dtos.Responses.GameConfigDto;
import intern.roima.hrmsbackend.dtos.Responses.GameDto;
import intern.roima.hrmsbackend.dtos.Responses.GameSlotDto;
import intern.roima.hrmsbackend.entities.Game_Module.SlotStatus;

public interface GameService {

    GameDto createGame(CreateGameRequest request, Long hrId);

    GameDto getGameById(Long gameId);

    GameDto updateGame(Long gameId, UpdateGameRequest request, Long hrId);

    void deleteGame(Long gameId, Long hrId);

    List<GameDto> getAllGames();

    GameConfigDto createGameConfig(CreateGameConfigRequest request, Long hrId);

    GameConfigDto getGameConfigById(Long configId);

    GameConfigDto getGameConfigByGameId(Long gameId);

    GameConfigDto updateGameConfig(Long configId, UpdateGameConfigRequest request, Long hrId);

    void deleteGameConfig(Long configId, Long hrId);

    List<GameConfigDto> getAllGameConfigs();

    List<GameSlotDto> generateSlots(Long gameId, GenerateSlotsRequest request, Long hrId);

    GameSlotDto getSlotById(Long slotId);

    GameSlotDto updateSlot(Long slotId, UpdateSlotRequest request, Long hrId);

    void deleteSlot(Long slotId, Long hrId);

    List<GameSlotDto> getSlotsByGame(Long gameId);

    List<GameSlotDto> getSlotsByDateRange(LocalDate startDate, LocalDate endDate);

    List<GameSlotDto> getSlotsByGameAndDate(Long gameId, LocalDate slotsdate);

    List<SlotStatus> getAllSlotStatuses();

}
