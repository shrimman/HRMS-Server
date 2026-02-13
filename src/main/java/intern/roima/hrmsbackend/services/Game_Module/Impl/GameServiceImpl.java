package intern.roima.hrmsbackend.services.Game_Module.Impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intern.roima.hrmsbackend.dtos.Requests.CreateGameConfigRequest;
import intern.roima.hrmsbackend.dtos.Requests.CreateGameRequest;
import intern.roima.hrmsbackend.dtos.Requests.GenerateSlotsRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateGameConfigRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateGameRequest;
import intern.roima.hrmsbackend.dtos.Requests.UpdateSlotRequest;
import intern.roima.hrmsbackend.dtos.Responses.GameConfigDto;
import intern.roima.hrmsbackend.dtos.Responses.GameDto;
import intern.roima.hrmsbackend.dtos.Responses.GameSlotDto;
import intern.roima.hrmsbackend.entities.Employee_Module.Employees;
import intern.roima.hrmsbackend.entities.Game_Module.GameConfigurations;
import intern.roima.hrmsbackend.entities.Game_Module.GameSlots;
import intern.roima.hrmsbackend.entities.Game_Module.Games;
import intern.roima.hrmsbackend.entities.Game_Module.SlotBookings;
import intern.roima.hrmsbackend.entities.Game_Module.SlotStatus;
import intern.roima.hrmsbackend.repositories.Employee_Module.EmployeeRepository;
import intern.roima.hrmsbackend.repositories.Game_Module.GameConfigurationsRepository;
import intern.roima.hrmsbackend.repositories.Game_Module.GameSlotsRepository;
import intern.roima.hrmsbackend.repositories.Game_Module.GamesRepository;
import intern.roima.hrmsbackend.repositories.Game_Module.SlotBookingsRepository;
import intern.roima.hrmsbackend.repositories.Game_Module.SlotParticipantsRepository;
import intern.roima.hrmsbackend.repositories.Game_Module.SlotStatusRepository;
import intern.roima.hrmsbackend.services.Game_Module.GameService;
import jakarta.persistence.EntityNotFoundException;

@Service
public class GameServiceImpl implements GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);
    private static final String SLOT_STATUS_AVAILABLE = "AVAILABLE";

    private final GamesRepository gamesRepository;
    private final GameConfigurationsRepository gameConfigurationsRepository;
    private final GameSlotsRepository gameSlotsRepository;
    private final EmployeeRepository employeeRepository;
    private final SlotStatusRepository slotStatusRepository;
    private final SlotBookingsRepository slotBookingsRepository;
    private final SlotParticipantsRepository slotParticipantsRepository;

    public GameServiceImpl(
            GamesRepository gamesRepository,
            GameConfigurationsRepository gameConfigurationsRepository,
            GameSlotsRepository gameSlotsRepository,
            EmployeeRepository employeeRepository,
            SlotStatusRepository slotStatusRepository,
            SlotBookingsRepository slotBookingsRepository,
            SlotParticipantsRepository slotParticipantsRepository) {
        this.gamesRepository = gamesRepository;
        this.gameConfigurationsRepository = gameConfigurationsRepository;
        this.gameSlotsRepository = gameSlotsRepository;
        this.employeeRepository = employeeRepository;
        this.slotStatusRepository = slotStatusRepository;
        this.slotBookingsRepository = slotBookingsRepository;
        this.slotParticipantsRepository = slotParticipantsRepository;
    }

    @Override
    @Transactional
    public GameDto createGame(CreateGameRequest request, Long hrId) {
        logger.info("Creating new game with name: {}", request.getGameName());

        Employees hrEmployee = employeeRepository.findById(hrId)
                .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrId));

        gamesRepository.findByGameName(request.getGameName())
                .ifPresent(game -> {
                    throw new IllegalArgumentException("Game with name '" + request.getGameName() + "' already exists");
                });

        Games game = new Games();
        game.setGameName(request.getGameName());
        game.setCreatedAt(LocalDateTime.now());
        game.setUpdatedAt(LocalDateTime.now());
        game.setUpdatedByEmployee(hrEmployee);

        Games savedGame = gamesRepository.save(game);
        logger.info("Successfully created game with ID: {}", savedGame.getGameId());

        return mapToGameDto(savedGame);
    }

    @Override
    @Transactional(readOnly = true)
    public GameDto getGameById(Long gameId) {
        logger.info("Fetching game with ID: {}", gameId);

        Games game = gamesRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with ID: " + gameId));

        return mapToGameDto(game);
    }

    @Override
    @Transactional
    public GameDto updateGame(Long gameId, UpdateGameRequest request, Long hrId) {
        logger.info("Updating game with ID: {}", gameId);

        Games game = gamesRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with ID: " + gameId));

        Employees hrEmployee = employeeRepository.findById(hrId)
                .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrId));

        if (request.getGameName() != null && !request.getGameName().equals(game.getGameName())) {
            gamesRepository.findByGameName(request.getGameName())
                    .ifPresent(existingGame -> {
                        throw new IllegalArgumentException(
                                "Game with name '" + request.getGameName() + "' already exists");
                    });
            game.setGameName(request.getGameName());
        }

        game.setUpdatedAt(LocalDateTime.now());
        game.setUpdatedByEmployee(hrEmployee);

        Games updatedGame = gamesRepository.save(game);
        logger.info("Successfully updated game with ID: {}", gameId);

        return mapToGameDto(updatedGame);
    }

    @Override
    @Transactional
    public void deleteGame(Long gameId, Long hrId) {
        logger.info("Deleting game with ID: {}", gameId);

        Games game = gamesRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with ID: " + gameId));

        List<GameSlots> existingSlots = gameSlotsRepository.findByGame_GameId(gameId);
        if (!existingSlots.isEmpty()) {
            throw new IllegalStateException("Cannot delete game with existing slots. Please delete all slots first.");
        }

        gameConfigurationsRepository.findByGame_GameId(gameId)
                .ifPresent(config -> gameConfigurationsRepository.delete(config));

        gamesRepository.delete(game);
        logger.info("Successfully deleted game with ID: {}", gameId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameDto> getAllGames() {
        logger.info("Fetching all games");

        List<Games> games = gamesRepository.findAll();
        return games.stream()
                .map(this::mapToGameDto)
                .toList();
    }

    @Override
    @Transactional
    public GameConfigDto createGameConfig(CreateGameConfigRequest request, Long hrId) {
        logger.info("Creating game configuration for game ID: {}", request.getGameId());

        Games game = gamesRepository.findById(request.getGameId())
                .orElseThrow(() -> new EntityNotFoundException("Game not found with ID: " + request.getGameId()));

        gameConfigurationsRepository.findByGame_GameId(request.getGameId())
                .ifPresent(config -> {
                    throw new IllegalStateException(
                            "Game configuration already exists for game ID: " + request.getGameId());
                });

        Employees hrEmployee = employeeRepository.findById(hrId)
                .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrId));

        if (request.getEndTime().isBefore(request.getStartTime())
                || request.getEndTime().equals(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        GameConfigurations config = new GameConfigurations();
        config.setGame(game);
        config.setGameDuration(request.getGameDuration());
        config.setMaxPlayers(request.getMaxPlayers());
        config.setStartTime(request.getStartTime());
        config.setEndTime(request.getEndTime());
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedByEmployee(hrEmployee);

        GameConfigurations savedConfig = gameConfigurationsRepository.save(config);
        logger.info("Successfully created game configuration with ID: {}", savedConfig.getConfigId());

        return mapToGameConfigDto(savedConfig);
    }

    @Override
    @Transactional(readOnly = true)
    public GameConfigDto getGameConfigById(Long configId) {
        logger.info("Fetching game configuration with ID: {}", configId);

        GameConfigurations config = gameConfigurationsRepository.findById(configId)
                .orElseThrow(() -> new EntityNotFoundException("Game configuration not found with ID: " + configId));

        return mapToGameConfigDto(config);
    }

    @Override
    @Transactional(readOnly = true)
    public GameConfigDto getGameConfigByGameId(Long gameId) {
        logger.info("Fetching game configuration for game ID: {}", gameId);

        GameConfigurations config = gameConfigurationsRepository.findByGame_GameId(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game configuration not found for game ID: " + gameId));

        return mapToGameConfigDto(config);
    }

    @Override
    @Transactional
    public GameConfigDto updateGameConfig(Long configId, UpdateGameConfigRequest request, Long hrId) {
        logger.info("Updating game configuration with ID: {}", configId);

        GameConfigurations config = gameConfigurationsRepository.findById(configId)
                .orElseThrow(() -> new EntityNotFoundException("Game configuration not found with ID: " + configId));

        Employees hrEmployee = employeeRepository.findById(hrId)
                .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrId));

        if (request.getGameDuration() != null) {
            config.setGameDuration(request.getGameDuration());
        }
        if (request.getMaxPlayers() != null) {
            config.setMaxPlayers(request.getMaxPlayers());
        }
        if (request.getStartTime() != null) {
            config.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            config.setEndTime(request.getEndTime());
        }

        if (config.getEndTime().isBefore(config.getStartTime()) || config.getEndTime().equals(config.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedByEmployee(hrEmployee);

        GameConfigurations updatedConfig = gameConfigurationsRepository.save(config);
        logger.info("Successfully updated game configuration with ID: {}", configId);

        return mapToGameConfigDto(updatedConfig);
    }

    @Override
    @Transactional
    public void deleteGameConfig(Long configId, Long hrId) {
        logger.info("Deleting game configuration with ID: {}", configId);

        GameConfigurations config = gameConfigurationsRepository.findById(configId)
                .orElseThrow(() -> new EntityNotFoundException("Game configuration not found with ID: " + configId));

        List<GameSlots> existingSlots = gameSlotsRepository.findByGame_GameId(config.getGame().getGameId());
        if (!existingSlots.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete game configuration with existing slots. Please delete all slots first.");
        }

        gameConfigurationsRepository.delete(config);
        logger.info("Successfully deleted game configuration with ID: {}", configId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameConfigDto> getAllGameConfigs() {
        logger.info("Fetching all game configurations");

        List<GameConfigurations> configs = gameConfigurationsRepository.findAll();
        return configs.stream()
                .map(this::mapToGameConfigDto)
                .toList();
    }

    @Override
    @Transactional
    public List<GameSlotDto> generateSlots(Long gameId, GenerateSlotsRequest request, Long hrId) {
        logger.info("Generating slots for game ID: {} from {} to {}", gameId, request.getStartDate(),
                request.getEndDate());

        Games game = gamesRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with ID: " + gameId));

        GameConfigurations config = gameConfigurationsRepository.findByGame_GameId(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game configuration not found for game ID: " + gameId));

        Employees hrEmployee = employeeRepository.findById(hrId)
                .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrId));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after or equal to start date");
        }

        SlotStatus availableStatus = slotStatusRepository.findByStatusName(SLOT_STATUS_AVAILABLE)
                .orElseThrow(() -> new EntityNotFoundException("Slot status 'AVAILABLE' not found"));

        List<GameSlots> generatedSlots = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();

        while (!currentDate.isAfter(request.getEndDate())) {
            LocalTime currentTime = config.getStartTime();

            while (currentTime.plusMinutes(config.getGameDuration()).isBefore(config.getEndTime())
                    || currentTime.plusMinutes(config.getGameDuration()).equals(config.getEndTime())) {

                LocalDateTime slotStart = LocalDateTime.of(currentDate, currentTime);
                LocalDateTime slotEnd = slotStart.plusMinutes(config.getGameDuration());

                GameSlots slot = new GameSlots();
                slot.setGame(game);
                slot.setSlotDate(currentDate);
                slot.setStartDateTime(slotStart);
                slot.setEndDateTime(slotEnd);
                slot.setMaxPlayers(config.getMaxPlayers());
                slot.setStatus(availableStatus);
                slot.setCreatedAt(LocalDateTime.now());
                slot.setUpdatedAt(LocalDateTime.now());
                slot.setUpdatedByEmployee(hrEmployee);

                generatedSlots.add(slot);
                currentTime = currentTime.plusMinutes(config.getGameDuration());
            }

            currentDate = currentDate.plusDays(1);
        }

        List<GameSlots> savedSlots = gameSlotsRepository.saveAll(generatedSlots);
        logger.info("Successfully generated {} slots for game ID: {}", savedSlots.size(), gameId);

        return savedSlots.stream()
                .map(this::mapToGameSlotDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GameSlotDto getSlotById(Long slotId) {
        logger.info("Fetching slot with ID: {}", slotId);

        GameSlots slot = gameSlotsRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Game slot not found with ID: " + slotId));

        return mapToGameSlotDto(slot);
    }

    @Override
    @Transactional
    public GameSlotDto updateSlot(Long slotId, UpdateSlotRequest request, Long hrId) {
        logger.info("Updating slot with ID: {}", slotId);

        GameSlots slot = gameSlotsRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Game slot not found with ID: " + slotId));

        Employees hrEmployee = employeeRepository.findById(hrId)
                .orElseThrow(() -> new EntityNotFoundException("HR employee not found with ID: " + hrId));

        if (request.getMaxPlayers() != null) {
            Long currentParticipants = slotParticipantsRepository.countActiveParticipantsBySlot(slotId);
            if (request.getMaxPlayers() < currentParticipants) {
                throw new IllegalArgumentException(
                        "Cannot set max players below current participant count: " + currentParticipants);
            }
            slot.setMaxPlayers(request.getMaxPlayers());
        }
        if (request.getSlotStatusId() != null) {
            SlotStatus status = slotStatusRepository.findById(request.getSlotStatusId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Slot status not found with ID: " + request.getSlotStatusId()));
            slot.setStatus(status);
        }

        slot.setUpdatedAt(LocalDateTime.now());
        slot.setUpdatedByEmployee(hrEmployee);

        GameSlots updatedSlot = gameSlotsRepository.save(slot);
        logger.info("Successfully updated slot with ID: {}", slotId);

        return mapToGameSlotDto(updatedSlot);
    }

    @Override
    @Transactional
    public void deleteSlot(Long slotId, Long hrId) {
        logger.info("Deleting slot with ID: {}", slotId);

        GameSlots slot = gameSlotsRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("Game slot not found with ID: " + slotId));

        List<SlotBookings> activeBookings = slotBookingsRepository.findActiveBookingsBySlot(slotId);
        if (!activeBookings.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete slot with active bookings. Please cancel all bookings first.");
        }

        gameSlotsRepository.delete(slot);
        logger.info("Successfully deleted slot with ID: {}", slotId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSlotDto> getSlotsByGame(Long gameId) {
        logger.info("Fetching all slots for game ID: {}", gameId);

        gamesRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with ID: " + gameId));

        List<GameSlots> slots = gameSlotsRepository.findByGame_GameId(gameId);
        return slots.stream()
                .map(this::mapToGameSlotDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSlotDto> getSlotsByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.info("Fetching slots from {} to {}", startDate, endDate);
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after or equal to start date");
        }

        List<GameSlots> slots = gameSlotsRepository.findByStartDateTimeBetween(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59));

        return slots.stream()
                .map(this::mapToGameSlotDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSlotDto> getSlotsByGameAndDate(Long gameId, LocalDate slotsdate) {
        logger.info("Fetching slots for game ID: {} on date: {}", gameId, slotsdate);

        gamesRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with ID: " + gameId));

        List<GameSlots> slots = gameSlotsRepository.findByGameAndDate(gameId, slotsdate);
        return slots.stream()
                .map(this::mapToGameSlotDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotStatus> getAllSlotStatuses() {
        logger.info("Fetching all slot statuses");
        return slotStatusRepository.findAll();
    }

    private GameDto mapToGameDto(Games game) {
        GameDto dto = new GameDto();
        dto.setGameId(game.getGameId());
        dto.setGameName(game.getGameName());
        dto.setCreatedAt(game.getCreatedAt());
        dto.setUpdatedAt(game.getUpdatedAt());
        if (game.getUpdatedByEmployee() != null) {
            dto.setUpdatedByEmployeeId(game.getUpdatedByEmployee().getEmployeeId());
        }
        return dto;
    }

    private GameConfigDto mapToGameConfigDto(GameConfigurations config) {
        GameConfigDto dto = new GameConfigDto();
        dto.setConfigId(config.getConfigId());
        dto.setGameId(config.getGame().getGameId());
        dto.setGameName(config.getGame().getGameName());
        dto.setGameDuration(config.getGameDuration());
        dto.setMaxPlayers(config.getMaxPlayers());
        dto.setStartTime(config.getStartTime());
        dto.setEndTime(config.getEndTime());
        dto.setCreatedAt(config.getCreatedAt());
        dto.setUpdatedAt(config.getUpdatedAt());
        if (config.getUpdatedByEmployee() != null) {
            dto.setUpdatedByEmployeeId(config.getUpdatedByEmployee().getEmployeeId());
        }
        return dto;
    }

    private GameSlotDto mapToGameSlotDto(GameSlots slot) {
        GameSlotDto dto = new GameSlotDto();
        dto.setSlotId(slot.getSlotId());
        dto.setGameId(slot.getGame().getGameId());
        dto.setGameName(slot.getGame().getGameName());
        dto.setSlotDate(slot.getSlotDate());
        dto.setStartDateTime(slot.getStartDateTime());
        dto.setEndDateTime(slot.getEndDateTime());
        dto.setMaxPlayers(slot.getMaxPlayers());

        Long currentParticipants = slotParticipantsRepository.countActiveParticipantsBySlot(slot.getSlotId());
        dto.setCurrentParticipants(currentParticipants.intValue());
        dto.setAvailableSpots(slot.getMaxPlayers() - currentParticipants.intValue());

        dto.setSlotStatusId(slot.getStatus().getSlotStatusId());
        dto.setSlotStatusName(slot.getStatus().getStatusName());
        dto.setCreatedAt(slot.getCreatedAt());
        dto.setUpdatedAt(slot.getUpdatedAt());
        if (slot.getUpdatedByEmployee() != null) {
            dto.setUpdatedByEmployeeId(slot.getUpdatedByEmployee().getEmployeeId());
        }
        return dto;
    }
}
