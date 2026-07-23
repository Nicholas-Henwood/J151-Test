package com.vzap.trytons.dto.scoring;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoringRuleRequestDTO {
    private UUID ruleId;

    @NotBlank(message = "Event type is required")
    @Size(max = 50, message = "Event type cannot exceed 50 characters")
    private String eventType;

    private int pointsAwarded;
    @NotBlank(message = "Season is required")
    @Size(max = 20, message = "Season cannot exceed 20 characters")
    private String season;

    private boolean active;

    Boolean isDeduction;
    String description;

}