package com.govtech.chinesescramble.dto.request;

import com.govtech.chinesescramble.entity.enums.DifficultyLevel;
import jakarta.validation.constraints.NotNull;

/**
 * Game start request DTO
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public record GameStartRequest(
    @NotNull(message = "Difficulty level is required")
    DifficultyLevel difficulty
) {
}