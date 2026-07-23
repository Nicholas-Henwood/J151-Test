package com.vzap.trytons.model.results;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStatisticsCorrection {
    private UUID correctionId;
    private UUID statId;
    private UUID correctionByAdminUserId;

    private String reason;

    private Map<String , Object > oldValuesJson;
    private Map<String , Object > newValuesJson;

    private LocalDateTime correctionTime;
}
