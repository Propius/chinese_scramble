package com.govtech.chinesescramble.controller;

import com.govtech.chinesescramble.dto.request.PlayerRegistrationRequest;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PlayerController - REST API for player management
 *
 * Endpoints:
 * - POST /api/players/register - Register new player
 * - GET /api/players/{id} - Get player by ID
 * - GET /api/players/{id}/statistics - Get player statistics
 * - PUT /api/players/{id}/profile - Update profile
 * - PUT /api/players/{id}/password - Change password
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PlayerController {

    private final PlayerService playerService;

    /**
     * Registers a new player
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerPlayer(@Valid @RequestBody PlayerRegistrationRequest request) {
        try {
            Player player = playerService.registerPlayer(
                request.username(),
                request.email(),
                request.password()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", player.getId(),
                "username", player.getUsername(),
                "email", player.getEmail(),
                "role", player.getRole().name(),
                "active", player.isActive(),
                "message", "Player registered successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets player by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlayer(@PathVariable Long id) {
        return playerService.getPlayerById(id)
            .map(player -> {
                var response = new java.util.HashMap<String, Object>();
                response.put("id", player.getId());
                response.put("username", player.getUsername());
                response.put("email", player.getEmail());
                response.put("role", player.getRole().name());
                response.put("active", player.isActive());
                if (player.getLastLogin() != null) {
                    response.put("lastLogin", player.getLastLogin().toString());
                }
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Gets player statistics
     */
    @GetMapping("/{id}/statistics")
    public ResponseEntity<?> getPlayerStatistics(@PathVariable Long id) {
        var stats = playerService.getPlayerStatistics(id);
        return ResponseEntity.ok(stats);
    }
}