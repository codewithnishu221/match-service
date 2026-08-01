package match.service.service;

import match.service.dto.MatchScoreRequest;
import match.service.dto.MatchScoreResponse;

public interface MatchService {

     MatchScoreResponse calculateMatchScore(MatchScoreRequest request, String authToken);
}
