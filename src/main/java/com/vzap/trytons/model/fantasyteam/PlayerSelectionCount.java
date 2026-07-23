package com.vzap.trytons.model.fantasyteam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlayerSelectionCount {
    private UUID playerId;

    private long selectionCount;
}
