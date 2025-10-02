package com.govtech.chinesescramble;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for Chinese Word Scramble Game.
 *
 * This application provides:
 * - Chinese Idiom Scramble Game (成语拼字游戏)
 * - Chinese Sentence Crafting Game (造句游戏)
 * - Leaderboard and Statistics
 * - Multi-level Hint System
 * - Achievement System
 *
 * Technical Stack:
 * - Spring Boot 3.2.2
 * - Java 21
 * - H2 Database (dev) / PostgreSQL (prod)
 * - JWT Authentication
 * - Caffeine Caching
 *
 * @author Elite Backend Lead Developer Agent
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling  // Enable scheduled tasks for configuration hot-reload
public class ChineseScrambleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChineseScrambleApplication.class, args);
    }
}