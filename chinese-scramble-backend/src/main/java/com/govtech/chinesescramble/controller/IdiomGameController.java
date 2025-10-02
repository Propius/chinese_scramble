package com.govtech.chinesescramble.controller;

import com.govtech.chinesescramble.dto.request.AnswerSubmissionRequest;
import com.govtech.chinesescramble.dto.request.GameStartRequest;
import com.govtech.chinesescramble.service.IdiomGameService;
import com.govtech.chinesescramble.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * IdiomGameController - REST API for idiom scramble game
 *
 * Endpoints:
 * - POST /api/games/idiom/start - Start new idiom game
 * - POST /api/games/idiom/submit - Submit answer (accepts player ID or username)
 * - POST /api/games/idiom/hint/{level} - Get hint (accepts player ID or username)
 * - GET /api/games/idiom/history/{playerId} - Get player history
 * - GET /api/games/idiom/best/{playerId}/{difficulty} - Get personal best
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/idiom-game")
@RequiredArgsConstructor
@Slf4j
public class IdiomGameController {

    private final IdiomGameService idiomGameService;
    private final PlayerService playerService;

    /**
     * Starts a new idiom game
     */
    @GetMapping("/start")
    public ResponseEntity<?> startGame(
        @RequestParam String difficulty,
        @RequestParam(required = false) String playerId
    ) {
        try {
            // Resolve player ID from playerId parameter or use default
            Long playerIdLong = 1L;
            if (playerId != null && !playerId.isEmpty()) {
                try {
                    playerIdLong = Long.parseLong(playerId);
                } catch (NumberFormatException e) {
                    // It's a username, look up or create the player
                    var player = playerService.getPlayerByUsername(playerId);
                    if (player.isEmpty()) {
                        // Auto-create player with this username
                        try {
                            var newPlayer = playerService.registerPlayer(playerId, playerId + "@game.local", "password123");
                            playerIdLong = newPlayer.getId();
                            log.info("Auto-created player: {} with ID: {}", playerId, playerIdLong);
                        } catch (Exception ex) {
                            log.error("Failed to auto-create player: {}", playerId, ex);
                            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create player: " + playerId));
                        }
                    } else {
                        playerIdLong = player.get().getId();
                    }
                }
            }

            var difficultyLevel = com.govtech.chinesescramble.entity.enums.DifficultyLevel.valueOf(difficulty.toUpperCase());
            var gameState = idiomGameService.startGame(playerIdLong, difficultyLevel);
            return ResponseEntity.ok(gameState);
        } catch (Exception e) {
            log.error("Failed to start idiom game", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Submits answer (accepts player ID or username)
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitAnswer(
        @RequestParam String playerId,
        @Valid @RequestBody AnswerSubmissionRequest request
    ) {
        try {
            // Try to parse as Long first (player ID)
            Long playerIdLong;
            try {
                playerIdLong = Long.parseLong(playerId);
            } catch (NumberFormatException e) {
                // It's a username, look up or create the player
                var player = playerService.getPlayerByUsername(playerId);
                if (player.isEmpty()) {
                    // Auto-create player with this username
                    try {
                        var newPlayer = playerService.registerPlayer(playerId, playerId + "@game.local", "password123");
                        playerIdLong = newPlayer.getId();
                        log.info("Auto-created player: {} with ID: {}", playerId, playerIdLong);
                    } catch (Exception ex) {
                        log.error("Failed to auto-create player: {}", playerId, ex);
                        return ResponseEntity.badRequest().body(Map.of("error", "Failed to create player: " + playerId));
                    }
                } else {
                    playerIdLong = player.get().getId();
                }
            }

            var result = idiomGameService.submitAnswer(
                playerIdLong,
                request.answer(),
                request.timeTaken(),
                request.hintsUsed()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to submit idiom answer", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets hint (accepts player ID or username)
     */
    @PostMapping("/hint/{level}")
    public ResponseEntity<?> getHint(
        @RequestParam String playerId,
        @PathVariable Integer level
    ) {
        try {
            // Try to parse as Long first (player ID)
            Long playerIdLong;
            try {
                playerIdLong = Long.parseLong(playerId);
            } catch (NumberFormatException e) {
                // It's a username, look up or create the player
                var player = playerService.getPlayerByUsername(playerId);
                if (player.isEmpty()) {
                    // Auto-create player with this username
                    try {
                        var newPlayer = playerService.registerPlayer(playerId, playerId + "@game.local", "password123");
                        playerIdLong = newPlayer.getId();
                        log.info("Auto-created player: {} with ID: {}", playerId, playerIdLong);
                    } catch (Exception ex) {
                        log.error("Failed to auto-create player: {}", playerId, ex);
                        return ResponseEntity.badRequest().body(Map.of("error", "Failed to create player: " + playerId));
                    }
                } else {
                    playerIdLong = player.get().getId();
                }
            }

            var hint = idiomGameService.getHint(playerIdLong, level);
            return ResponseEntity.ok(hint);
        } catch (Exception e) {
            log.error("Failed to get hint", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets player history
     */
    @GetMapping("/history/{playerId}")
    public ResponseEntity<?> getHistory(@PathVariable Long playerId) {
        var history = idiomGameService.getPlayerHistory(playerId, 10);
        return ResponseEntity.ok(history);
    }
}