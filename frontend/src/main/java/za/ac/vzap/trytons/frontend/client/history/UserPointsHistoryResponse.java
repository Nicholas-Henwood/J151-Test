package za.ac.vzap.trytons.frontend.client.history;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class UserPointsHistoryResponse {

    private int totals;
    private List<WeeklyPerformanceResponse> rounds;
    private Integer ranking;

}