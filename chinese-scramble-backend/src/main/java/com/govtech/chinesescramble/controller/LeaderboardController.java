package com.govtech.chinesescramble.controller;

import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import com.govtech.chinesescramble.entity.enums.GameType;
import com.govtech.chinesescramble.service.LeaderboardService;
import com.govtech.chinesescramble.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * LeaderboardController - REST API for leaderboard rankings
 *
 * Endpoints:
 * - GET /api/leaderboards/top - Get top players by game type and difficulty
 * - GET /api/leaderboards/player/{playerIdOrUsername} - Get player's rankings across all boards
 * - GET /api/leaderboards/player/{playerIdOrUsername}/rank - Get player's rank for specific board
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/leaderboards")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final PlayerService playerService;

    /**
     * Gets top players for specific game type and difficulty
     */
    @GetMapping("/top")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTopPlayers(
        @RequestParam GameType gameType,
        @RequestParam DifficultyLevel difficulty,
        @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            var leaderboards = leaderboardService.getTopPlayers(gameType, difficulty, limit);

            var response = leaderboards.stream()
                .map(lb -> Map.of(
                    "rank", lb.getRank(),
                    "playerId", lb.getPlayer().getId(),
                    "username", lb.getPlayer().getUsername(),
                    "bestScore", lb.getBestScore(),
                    "averageScore", lb.getAverageScore(),
                    "totalGames", lb.getTotalGames(),
                    "lastPlayed", lb.getLastPlayed().toString()
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch leaderboard", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets all rankings for a specific player (by ID or username)
     */
    @GetMapping("/player/{playerIdOrUsername}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPlayerRankings(@PathVariable String playerIdOrUsername) {
        try {
            // Try to parse as Long first (player ID)
            Long playerId;
            try {
                playerId = Long.parseLong(playerIdOrUsername);
            } catch (NumberFormatException e) {
                // It's a username, look up or create the player
                var player = playerService.getPlayerByUsername(playerIdOrUsername);
                if (player.isEmpty()) {
                    // Auto-create player with this username
                    try {
                        var newPlayer = playerService.registerPlayer(playerIdOrUsername, playerIdOrUsername + "@game.local", "password123");
                        playerId = newPlayer.getId();
                        log.info("Auto-created player: {} with ID: {}", playerIdOrUsername, playerId);
                    } catch (Exception ex) {
                        log.error("Failed to auto-create player: {}", playerIdOrUsername, ex);
                        return ResponseEntity.badRequest().body(Map.of("error", "Failed to create player: " + playerIdOrUsername));
                    }
                } else {
                    playerId = player.get().getId();
                }
            }

            var rankings = leaderboardService.getPlayerRankings(playerId);

            var response = rankings.stream()
                .map(lb -> Map.of(
                    "gameType", lb.getGameType().name(),
                    "difficulty", lb.getDifficulty().name(),
                    "rank", lb.getRank(),
                    "bestScore", lb.getBestScore(),
                    "averageScore", lb.getAverageScore(),
                    "totalGames", lb.getTotalGames()
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch player rankings", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets player's rank for specific board (by ID or username)
     */
    @GetMapping("/player/{playerIdOrUsername}/rank")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getPlayerRank(
        @PathVariable String playerIdOrUsername,
        @RequestParam GameType gameType,
        @RequestParam DifficultyLevel difficulty
    ) {
        try {
            // Try to parse as Long first (player ID)
            Long playerId;
            try {
                playerId = Long.parseLong(playerIdOrUsername);
            } catch (NumberFormatException e) {
                // It's a username, look up or create the player
                var player = playerService.getPlayerByUsername(playerIdOrUsername);
                if (player.isEmpty()) {
                    // Auto-create player with this username
                    try {
                        var newPlayer = playerService.registerPlayer(playerIdOrUsername, playerIdOrUsername + "@game.local", "password123");
                        playerId = newPlayer.getId();
                        log.info("Auto-created player: {} with ID: {}", playerIdOrUsername, playerId);
                    } catch (Exception ex) {
                        log.error("Failed to auto-create player: {}", playerIdOrUsername, ex);
                        return ResponseEntity.badRequest().body(Map.of("error", "Failed to create player: " + playerIdOrUsername));
                    }
                } else {
                    playerId = player.get().getId();
                }
            }

            var leaderboard = leaderboardService.getPlayerRank(playerId, gameType, difficulty);

            if (leaderboard.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "message", "Player has not played this difficulty yet",
                    "rank", null
                ));
            }

            var lb = leaderboard.get();
            return ResponseEntity.ok(Map.of(
                "playerId", playerId,
                "gameType", gameType.name(),
                "difficulty", difficulty.name(),
                "rank", lb.getRank(),
                "bestScore", lb.getBestScore(),
                "averageScore", lb.getAverageScore(),
                "totalGames", lb.getTotalGames()
            ));
        } catch (Exception e) {
            log.error("Failed to fetch player rank", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
