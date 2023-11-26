package searchengine.dto.statistics;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data

public class StatisticsResponse {
    private boolean result;
    private StatisticsData statistics;
}
