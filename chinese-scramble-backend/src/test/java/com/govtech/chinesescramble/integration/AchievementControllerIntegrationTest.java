package com.govtech.chinesescramble.integration;

import com.govtech.chinesescramble.entity.Achievement;
import com.govtech.chinesescramble.entity.Player;
import com.govtech.chinesescramble.entity.enums.UserRole;
import com.govtech.chinesescramble.repository.AchievementRepository;
import com.govtech.chinesescramble.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for AchievementController
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AchievementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    private Player testPlayer;

    @BeforeEach
    void setUp() {
        achievementRepository.deleteAll();
        playerRepository.deleteAll();

        LocalDateTime now = LocalDateTime.now();
        testPlayer = Player.builder()
            .username("achievementuser")
            .email("achievement@test.com")
            .passwordHash("hashedpassword")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        testPlayer.setCreatedAt(now);
        testPlayer.setUpdatedAt(now);
        testPlayer = playerRepository.save(testPlayer);

        // Create some test achievements
        Achievement achievement1 = Achievement.builder()
            .player(testPlayer)
            .achievementType("FIRST_WIN")
            .title("首次胜利")
            .description("完成第一场游戏")
            .unlockedAt(now)
            .metadata("{\"score\": 100}")
            .build();
        achievement1.setCreatedAt(now);
        achievement1.setUpdatedAt(now);
        achievementRepository.save(achievement1);

        Achievement achievement2 = Achievement.builder()
            .player(testPlayer)
            .achievementType("PERFECT_SCORE")
            .title("完美得分")
            .description("达到100%准确率")
            .unlockedAt(null) // Not yet unlocked
            .metadata(null)
            .build();
        achievement2.setCreatedAt(now);
        achievement2.setUpdatedAt(now);
        achievementRepository.save(achievement2);
    }

    @Test
    void testGetPlayerAchievements_Success() throws Exception {
        mockMvc.perform(get("/api/achievements/player/" + testPlayer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].id").exists())
            .andExpect(jsonPath("$[0].type").exists())
            .andExpect(jsonPath("$[0].title").exists())
            .andExpect(jsonPath("$[0].description").exists());
    }

    @Test
    void testGetPlayerAchievements_PlayerNotFound() throws Exception {
        mockMvc.perform(get("/api/achievements/player/99999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetUnlockedAchievements_Success() throws Exception {
        mockMvc.perform(get("/api/achievements/player/" + testPlayer.getId() + "/unlocked"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").exists())
            .andExpect(jsonPath("$.achievements").isArray())
            .andExpect(jsonPath("$.achievements[0].type").exists())
            .andExpect(jsonPath("$.achievements[0].unlockedAt").exists());
    }

    @Test
    void testGetUnlockedAchievements_NoUnlocked() throws Exception {
        // Create player with no unlocked achievements
        LocalDateTime now = LocalDateTime.now();
        Player newPlayer = Player.builder()
            .username("newuser")
            .email("new@test.com")
            .passwordHash("hashedpassword")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        newPlayer.setCreatedAt(now);
        newPlayer.setUpdatedAt(now);
        newPlayer = playerRepository.save(newPlayer);

        mockMvc.perform(get("/api/achievements/player/" + newPlayer.getId() + "/unlocked"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(0))
            .andExpect(jsonPath("$.achievements").isArray())
            .andExpect(jsonPath("$.achievements").isEmpty());
    }

    @Test
    void testGetAchievementProgress_Success() throws Exception {
        mockMvc.perform(get("/api/achievements/player/" + testPlayer.getId() + "/progress"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalAchievements").exists())
            .andExpect(jsonPath("$.unlockedCount").exists())
            .andExpect(jsonPath("$.lockedCount").exists())
            .andExpect(jsonPath("$.completionRate").exists());
    }

    @Test
    void testGetAchievementProgress_NoAchievements() throws Exception {
        // Create player with no achievements
        LocalDateTime now = LocalDateTime.now();
        Player newPlayer = Player.builder()
            .username("noachievements")
            .email("noach@test.com")
            .passwordHash("hashedpassword")
            .role(UserRole.PLAYER)
            .active(true)
            .build();
        newPlayer.setCreatedAt(now);
        newPlayer.setUpdatedAt(now);
        newPlayer = playerRepository.save(newPlayer);

        mockMvc.perform(get("/api/achievements/player/" + newPlayer.getId() + "/progress"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalAchievements").value(0))
            .andExpect(jsonPath("$.unlockedCount").value(0))
            .andExpect(jsonPath("$.lockedCount").value(0))
            .andExpect(jsonPath("$.completionRate").value("0.0%"));
    }

    @Test
    void testGetAllAchievements_Success() throws Exception {
        mockMvc.perform(get("/api/achievements/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.FIRST_WIN").exists())
            .andExpect(jsonPath("$.FIRST_WIN.title").value("首次胜利"))
            .andExpect(jsonPath("$.SPEED_DEMON").exists())
            .andExpect(jsonPath("$.SPEED_DEMON.title").value("速度恶魔"))
            .andExpect(jsonPath("$.PERFECT_SCORE").exists())
            .andExpect(jsonPath("$.HUNDRED_GAMES").exists())
            .andExpect(jsonPath("$.IDIOM_MASTER").exists())
            .andExpect(jsonPath("$.SENTENCE_MASTER").exists())
            .andExpect(jsonPath("$.TOP_RANKED").exists())
            .andExpect(jsonPath("$.CONSISTENCY").exists())
            .andExpect(jsonPath("$.HIGH_SCORER").exists())
            .andExpect(jsonPath("$.HINT_FREE").exists());
    }

    @Test
    void testGetAllAchievements_VerifyDescriptions() throws Exception {
        mockMvc.perform(get("/api/achievements/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.FIRST_WIN.description").value("完成第一场游戏"))
            .andExpect(jsonPath("$.SPEED_DEMON.description").value("在30秒内完成游戏"))
            .andExpect(jsonPath("$.PERFECT_SCORE.description").value("达到100%准确率且不使用提示"));
    }

    @Test
    void testGetPlayerAchievements_MultipleAchievements() throws Exception {
        // Add more achievements
        LocalDateTime now = LocalDateTime.now();
        Achievement achievement3 = Achievement.builder()
            .player(testPlayer)
            .achievementType("SPEED_DEMON")
            .title("速度恶魔")
            .description("在30秒内完成")
            .unlockedAt(now)
            .metadata("{\"time\": 28}")
            .build();
        achievement3.setCreatedAt(now);
        achievement3.setUpdatedAt(now);
        achievementRepository.save(achievement3);

        mockMvc.perform(get("/api/achievements/player/" + testPlayer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void testGetUnlockedAchievements_MultipleUnlocked() throws Exception {
        // Clear existing achievements first
        achievementRepository.deleteAll();

        // Add only unlocked achievements
        LocalDateTime now = LocalDateTime.now();
        Achievement achievement1 = Achievement.builder()
            .player(testPlayer)
            .achievementType("FIRST_WIN")
            .title("首次胜利")
            .description("完成第一场游戏")
            .unlockedAt(now)
            .metadata("{\"score\": 100}")
            .build();
        achievement1.setCreatedAt(now);
        achievement1.setUpdatedAt(now);
        achievementRepository.save(achievement1);

        Achievement achievement2 = Achievement.builder()
            .player(testPlayer)
            .achievementType("HIGH_SCORER")
            .title("高分达人")
            .description("单场超过1000分")
            .unlockedAt(now)
            .metadata("{\"score\": 1200}")
            .build();
        achievement2.setCreatedAt(now);
        achievement2.setUpdatedAt(now);
        achievementRepository.save(achievement2);

        mockMvc.perform(get("/api/achievements/player/" + testPlayer.getId() + "/unlocked"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.count").value(2))
            .andExpect(jsonPath("$.achievements").isArray())
            .andExpect(jsonPath("$.achievements.length()").value(2));
    }

    @Test
    void testGetAchievementProgress_FullCompletion() throws Exception {
        // Update all achievements to be unlocked
        LocalDateTime now = LocalDateTime.now();
        var achievements = achievementRepository.findByPlayerId(testPlayer.getId());
        for (Achievement achievement : achievements) {
            if (achievement.getUnlockedAt() == null) {
                achievement.setUnlockedAt(now);
                achievementRepository.save(achievement);
            }
        }

        mockMvc.perform(get("/api/achievements/player/" + testPlayer.getId() + "/progress"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalAchievements").value(2))
            .andExpect(jsonPath("$.unlockedCount").value(2))
            .andExpect(jsonPath("$.lockedCount").value(0))
            .andExpect(jsonPath("$.completionRate").value("100.0%"));
    }

    @Test
    void testCrossOriginHeaders() throws Exception {
        mockMvc.perform(get("/api/achievements/all")
                .header("Origin", "http://localhost:3000"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
