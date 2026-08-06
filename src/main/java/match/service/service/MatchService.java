package match.service.service;

import match.service.dto.MatchScoreRequest;
import match.service.dto.MatchScoreResponse;
import match.service.dto.ResumeGenerationRequest;
import match.service.dto.ResumeGenerationResponse;
import org.springframework.stereotype.Service;


public interface MatchService {

     MatchScoreResponse calculateMatchScore(MatchScoreRequest request, String authToken);
     ResumeGenerationResponse generateTailoredResume(ResumeGenerationRequest request, String authToken);
}
