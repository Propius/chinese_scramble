package com.govtech.chinesescramble.controller;

import com.govtech.chinesescramble.dto.request.AnswerSubmissionRequest;
import com.govtech.chinesescramble.dto.request.GameStartRequest;
import com.govtech.chinesescramble.service.SentenceGameService;
import com.govtech.chinesescramble.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SentenceGameController - REST API for sentence crafting game
 *
 * Endpoints:
 * - POST /api/games/sentence/start - Start new sentence game
 * - POST /api/games/sentence/submit - Submit answer (accepts player ID or username)
 * - POST /api/games/sentence/hint/{level} - Get hint (accepts player ID or username)
 * - GET /api/games/sentence/history/{playerId} - Get player history
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/sentence-game")
@RequiredArgsConstructor
@Slf4j
public class SentenceGameController {

    private final SentenceGameService sentenceGameService;
    private final PlayerService playerService;

    /**
     * Starts a new sentence crafting game
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
            var gameState = sentenceGameService.startGame(playerIdLong, difficultyLevel);
            return ResponseEntity.ok(gameState);
        } catch (Exception e) {
            log.error("Failed to start sentence game", e);
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

            var result = sentenceGameService.submitAnswer(
                playerIdLong,
                request.answer(),
                request.timeTaken(),
                request.hintsUsed()
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to submit sentence answer", e);
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

            var hint = sentenceGameService.getHint(playerIdLong, level);
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
        var history = sentenceGameService.getPlayerHistory(playerId, 10);
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

            // Clear question history for sentence game
            sentenceGameService.restartQuiz(playerIdLong);

            log.info("Quiz restarted for player: {}", playerIdLong);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "造句题目已重置，您可以重新开始挑战所有题目！"
            ));
        } catch (Exception e) {
            log.error("Failed to restart quiz", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
