package com.vzap.trytons.dto.history;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserPointsHistoryResponseDTO {
private int totals;

private List<WeeklyPerformanceResponseDTO> rounds;

private Integer ranking;
}