package com.vzap.trytons.model.fixture;

import com.vzap.trytons.enums.RoundLockAction;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundLock {
    private UUID lockId;
    private UUID roundId;

    private RoundLockAction lockAction;

    private UUID actionByAdminUserId;

    private LocalDateTime actionAt;

    private String reason;
}