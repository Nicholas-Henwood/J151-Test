package com.vzap.trytons.dto.league;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueMemberResponseDTO {
    private UUID membershipId;
    private UUID userId;

    private String userDisplayName;

    private UUID teamId;

    private String teamDisplayName;

    private LocalDateTime joinDate;

    private Boolean isActive;
}