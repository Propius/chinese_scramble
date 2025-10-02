package com.govtech.chinesescramble.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Answer submission request DTO
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public record AnswerSubmissionRequest(
    @NotBlank(message = "Answer is required")
    String answer,

    @NotNull(message = "Time taken is required")
    @Min(value = 1, message = "Time taken must be at least 1 second")
    Integer timeTaken,

    @NotNull(message = "Hints used count is required")
    @Min(value = 0, message = "Hints used cannot be negative")
    Integer hintsUsed
) {
}