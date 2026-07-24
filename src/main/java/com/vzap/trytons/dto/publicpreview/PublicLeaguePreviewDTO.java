package com.vzap.trytons.dto.publicpreview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Marketing-safe view of a PUBLIC league for the pre-auth landing page.
 * Deliberately omits identifiers and the invite code so nothing sensitive
 * (leagueId, managerUserId, leagueCode) leaks to unauthenticated visitors.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicLeaguePreviewDTO {
    private String leagueName;
    private String description;
    private int maxMembers;
    private int memberCount;
}
