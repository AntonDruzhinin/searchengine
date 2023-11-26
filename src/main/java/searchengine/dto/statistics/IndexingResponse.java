package searchengine.dto.statistics;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IndexingResponse{
    private Boolean result;
    private String response;
    private String error;
}
