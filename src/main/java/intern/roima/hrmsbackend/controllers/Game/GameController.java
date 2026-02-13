package intern.roima.hrmsbackend.controllers.Game;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import intern.roima.hrmsbackend.security.annotations.CurrentUser;
import intern.roima.hrmsbackend.services.Game_Module.GameService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<GameDto> createGame(
            @Valid @RequestBody CreateGameRequest request,
            @CurrentUser Long hrId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gameService.createGame(request, hrId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<GameDto>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    @GetMapping("/{gameId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<GameDto> getGameById(@PathVariable("gameId") Long gameId) {
        return ResponseEntity.ok(gameService.getGameById(gameId));
    }

    @PutMapping("/{gameId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<GameDto> updateGame(
            @PathVariable("gameId") Long gameId,
            @Valid @RequestBody UpdateGameRequest request,
            @CurrentUser Long hrId) {
        return ResponseEntity.ok(gameService.updateGame(gameId, request, hrId));
    }

    @DeleteMapping("/{gameId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> deleteGame(
            @PathVariable("gameId") Long gameId,
            @CurrentUser Long hrId) {
        gameService.deleteGame(gameId, hrId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{gameId}/config")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<GameConfigDto> createGameConfig(
            @PathVariable("gameId") Long gameId,
            @Valid @RequestBody CreateGameConfigRequest request,
            @CurrentUser Long hrId) {
        request.setGameId(gameId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gameService.createGameConfig(request, hrId));
    }

    @GetMapping("/configs")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<GameConfigDto>> getAllGameConfigs() {
        return ResponseEntity.ok(gameService.getAllGameConfigs());
    }

    @GetMapping("/configs/{configId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<GameConfigDto> getGameConfigById(@PathVariable("configId") Long configId) {
        return ResponseEntity.ok(gameService.getGameConfigById(configId));
    }

    @GetMapping("/{gameId}/config")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<GameConfigDto> getGameConfigByGameId(@PathVariable("gameId") Long gameId) {
        return ResponseEntity.ok(gameService.getGameConfigByGameId(gameId));
    }

    @PutMapping("/configs/{configId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<GameConfigDto> updateGameConfig(
            @PathVariable("configId") Long configId,
            @Valid @RequestBody UpdateGameConfigRequest request,
            @CurrentUser Long hrId) {
        return ResponseEntity.ok(gameService.updateGameConfig(configId, request, hrId));
    }

    @DeleteMapping("/configs/{configId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> deleteGameConfig(
            @PathVariable("configId") Long configId,
            @CurrentUser Long hrId) {
        gameService.deleteGameConfig(configId, hrId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{gameId}/slots/generate")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<GameSlotDto>> generateSlots(
            @PathVariable("gameId") Long gameId,
            @Valid @RequestBody GenerateSlotsRequest request,
            @CurrentUser Long hrId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gameService.generateSlots(gameId, request, hrId));
    }

    @GetMapping("/slots/{slotId}")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<GameSlotDto> getSlotById(@PathVariable("slotId") Long slotId) {
        return ResponseEntity.ok(gameService.getSlotById(slotId));
    }

    @PutMapping("/slots/{slotId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<GameSlotDto> updateSlot(
            @PathVariable("slotId") Long slotId,
            @Valid @RequestBody UpdateSlotRequest request,
            @CurrentUser Long hrId) {
        return ResponseEntity.ok(gameService.updateSlot(slotId, request, hrId));
    }

    @DeleteMapping("/slots/{slotId}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable("slotId") Long slotId,
            @CurrentUser Long hrId) {
        gameService.deleteSlot(slotId, hrId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{gameId}/slots")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<GameSlotDto>> getSlotsByGame(@PathVariable("gameId") Long gameId) {
        return ResponseEntity.ok(gameService.getSlotsByGame(gameId));
    }

    @GetMapping("/slots")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<GameSlotDto>> getSlotsByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(gameService.getSlotsByDateRange(startDate, endDate));
    }

    @GetMapping("/{gameId}/slots/date")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<GameSlotDto>> getSlotsByGameAndDate(
            @PathVariable("gameId") Long gameId,
            @RequestParam("slotsdate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate slotsdate) {
        return ResponseEntity.ok(gameService.getSlotsByGameAndDate(gameId, slotsdate));
    }

    @GetMapping("/slots/statuses")
    @PreAuthorize("hasAnyRole('HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<List<SlotStatus>> getAllSlotStatuses() {
        return ResponseEntity.ok(gameService.getAllSlotStatuses());
    }
}
