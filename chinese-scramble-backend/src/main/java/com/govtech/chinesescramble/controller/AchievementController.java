package com.govtech.chinesescramble.controller;

import com.govtech.chinesescramble.service.AchievementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * AchievementController - REST API for player achievements
 *
 * Endpoints:
 * - GET /api/achievements/player/{playerId} - Get all achievements for player
 * - GET /api/achievements/player/{playerId}/unlocked - Get unlocked achievements
 * - GET /api/achievements/player/{playerId}/progress - Get achievement progress
 * - GET /api/achievements/all - Get all available achievements
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AchievementController {

    private final AchievementService achievementService;

    /**
     * Gets all achievements for a player (unlocked and available)
     */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<?> getPlayerAchievements(@PathVariable Long playerId) {
        try {
            var achievements = achievementService.getPlayerAchievements(playerId);

            var response = achievements.stream()
                .map(a -> Map.of(
                    "id", a.getId(),
                    "type", a.getAchievementType(),
                    "title", a.getTitle(),
                    "description", a.getDescription(),
                    "unlockedAt", a.getUnlockedAt() != null ? a.getUnlockedAt().toString() : null,
                    "metadata", a.getMetadata() != null ? a.getMetadata() : ""
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch player achievements", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets only unlocked achievements for a player
     */
    @GetMapping("/player/{playerId}/unlocked")
    public ResponseEntity<?> getUnlockedAchievements(@PathVariable Long playerId) {
        try {
            var achievements = achievementService.getUnlockedAchievements(playerId);

            var response = achievements.stream()
                .map(a -> Map.of(
                    "id", a.getId(),
                    "type", a.getAchievementType(),
                    "title", a.getTitle(),
                    "description", a.getDescription(),
                    "unlockedAt", a.getUnlockedAt().toString(),
                    "metadata", a.getMetadata() != null ? a.getMetadata() : ""
                ))
                .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "count", achievements.size(),
                "achievements", response
            ));
        } catch (Exception e) {
            log.error("Failed to fetch unlocked achievements", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets achievement progress statistics for a player
     */
    @GetMapping("/player/{playerId}/progress")
    public ResponseEntity<?> getAchievementProgress(@PathVariable Long playerId) {
        try {
            var achievements = achievementService.getPlayerAchievements(playerId);
            long totalAchievements = achievements.size();
            long unlockedCount = achievements.stream()
                .filter(a -> a.getUnlockedAt() != null)
                .count();

            double completionRate = totalAchievements > 0
                ? (double) unlockedCount / totalAchievements * 100
                : 0.0;

            return ResponseEntity.ok(Map.of(
                "totalAchievements", totalAchievements,
                "unlockedCount", unlockedCount,
                "lockedCount", totalAchievements - unlockedCount,
                "completionRate", String.format("%.1f%%", completionRate)
            ));
        } catch (Exception e) {
            log.error("Failed to fetch achievement progress", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets all available achievement types with descriptions
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllAchievements() {
        var achievementTypes = Map.of(
            "FIRST_WIN", Map.of(
                "title", "首次胜利",
                "description", "完成第一场游戏"
            ),
            "SPEED_DEMON", Map.of(
                "title", "速度恶魔",
                "description", "在30秒内完成游戏"
            ),
            "PERFECT_SCORE", Map.of(
                "title", "完美得分",
                "description", "达到100%准确率且不使用提示"
            ),
            "HUNDRED_GAMES", Map.of(
                "title", "百场达人",
                "description", "完成100场游戏"
            ),
            "IDIOM_MASTER", Map.of(
                "title", "成语大师",
                "description", "在成语排行榜上排名第一"
            ),
            "SENTENCE_MASTER", Map.of(
                "title", "句子大师",
                "description", "在句子排行榜上排名第一"
            ),
            "TOP_RANKED", Map.of(
                "title", "顶级玩家",
                "description", "进入任何排行榜前10名"
            ),
            "CONSISTENCY", Map.of(
                "title", "坚持不懈",
                "description", "连续7天玩游戏"
            ),
            "HIGH_SCORER", Map.of(
                "title", "高分达人",
                "description", "单场游戏得分超过1000分"
            ),
            "HINT_FREE", Map.of(
                "title", "无提示挑战",
                "description", "不使用提示完成10场游戏"
            )
        );

        return ResponseEntity.ok(achievementTypes);
    }
}
