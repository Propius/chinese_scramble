package com.govtech.chinesescramble.entity;

/**
 * Difficulty levels for game questions.
 * <p>
 * Levels (简体中文):
 * - EASY: 简单 (Simple idioms/sentences, longer time limits)
 * - MEDIUM: 中等 (Moderate difficulty, standard time limits)
 * - HARD: 困难 (Complex idioms/sentences, shorter time limits)
 * - EXPERT: 专家 (Literary/classical content, minimal time limits)
 */
public enum DifficultyLevel {
    EASY,    // 简单
    MEDIUM,  // 中等
    HARD,    // 困难
    EXPERT   // 专家
}