package com.govtech.chinesescramble.controller;

import com.govtech.chinesescramble.dto.request.AnswerSubmissionRequest;
import com.govtech.chinesescramble.dto.request.GameStartRequest;
import com.govtech.chinesescramble.entity.Player;
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
@RequestMapping("/api/games/idiom")
@RequiredArgsConstructor
@Slf4j
public class IdiomGameController {

    private final IdiomGameService idiomGameService;
    private final PlayerService playerService;

    /**
     * Starts a new idiom game
     */
    @PostMapping("/start")
    public ResponseEntity<?> startGame(
        @RequestParam Long playerId,
        @RequestBody @Valid GameStartRequest request
    ) {
        try {
            log.info("Starting idiom game for player {} with difficulty {}", playerId, request.difficulty());
            var gameState = idiomGameService.startGame(playerId, request.difficulty());
            return ResponseEntity.ok(gameState);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for idiom game", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to start idiom game", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Submits answer (accepts player ID or username)
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitAnswer(
        @RequestParam(required = false) String playerId,
        @Valid @RequestBody AnswerSubmissionRequest request
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
                        } catch (IllegalArgumentException ex) {
                            // Player was created by another request, fetch it
                            if (ex.getMessage() != null && ex.getMessage().contains("already exists")) {
                                player = playerService.getPlayerByUsername(playerId);
                                playerIdLong = player.map(Player::getId).orElse(1L);
                                log.info("Player {} already exists with ID: {}", playerId, playerIdLong);
                            } else {
                                log.error("Failed to auto-create player: {}", playerId, ex);
                                return ResponseEntity.badRequest().body(Map.of("error", "Failed to create player: " + playerId));
                            }
                        }
                    } else {
                        playerIdLong = player.get().getId();
                    }
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
        @RequestParam(required = false) String playerId,
        @PathVariable Integer level
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
                        } catch (IllegalArgumentException ex) {
                            // Player was created by another request, fetch it
                            if (ex.getMessage() != null && ex.getMessage().contains("already exists")) {
                                player = playerService.getPlayerByUsername(playerId);
                                playerIdLong = player.map(Player::getId).orElse(1L);
                                log.info("Player {} already exists with ID: {}", playerId, playerIdLong);
                            } else {
                                log.error("Failed to auto-create player: {}", playerId, ex);
                                return ResponseEntity.badRequest().body(Map.of("error", "Failed to create player: " + playerId));
                            }
                        }
                    } else {
                        playerIdLong = player.get().getId();
                    }
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

    /**
     * Restarts quiz - clears question history for player
     * This allows the player to replay all questions from the beginning
     */
    @PostMapping("/restart")
    public ResponseEntity<?> restartQuiz(@RequestParam String playerId) {
        try {
            // Try to parse as Long first (player ID)
            Long playerIdLong;
            try {
                playerIdLong = Long.parseLong(playerId);
            } catch (NumberFormatException e) {
                // It's a username, look up player
                var player = playerService.getPlayerByUsername(playerId);
                if (player.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Player not found: " + playerId
                    ));
                }
                playerIdLong = player.get().getId();
            }

            // Clear question history for idiom game
            idiomGameService.restartQuiz(playerIdLong);

            log.info("Quiz restarted for player: {}", playerIdLong);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "成语题目已重置，您可以重新开始挑战所有题目！"
            ));
        } catch (Exception e) {
            log.error("Failed to restart quiz", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}