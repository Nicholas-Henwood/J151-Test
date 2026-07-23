package com.vzap.trytons.dto.results;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerStatisticsCorrectionResponseDTO {
    private UUID correctionId;
    private UUID statId;
    private UUID correctionByAdminUserId;

    private String reason;

    private Map<String, Object> oldValuesJson;
    private Map<String, Object> newValuesJson;

    private LocalDateTime correctionTime;
}
