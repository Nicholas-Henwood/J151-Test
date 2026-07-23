package com.vzap.trytons.dto.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerStatisticsCorrectionRequestDTO {
    private UUID statId;
    private UUID correctionByAdminUserId;

    private String reason;

    private Map<String, Object> newValuesJson;
}
