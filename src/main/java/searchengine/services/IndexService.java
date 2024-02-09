package searchengine.services;

import searchengine.dto.statistics.IndexingResponse;

public interface IndexService {
    IndexingResponse getIndexingResult();
    IndexingResponse getFalseResult();
    IndexingResponse getStopIndexingResponse();
}
