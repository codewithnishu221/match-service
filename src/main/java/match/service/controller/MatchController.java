package match.service.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import match.service.dto.MatchScoreRequest;
import match.service.dto.MatchScoreResponse;
import match.service.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    @PostMapping("/api/match/score")
    public ResponseEntity<MatchScoreResponse> createScore(@RequestBody MatchScoreRequest matchScoreRequest, HttpServletRequest httpServletRequest){
        String authHeader = httpServletRequest.getHeader("Authorization");
        MatchScoreResponse response = matchService.calculateMatchScore(matchScoreRequest, authHeader);

        return ResponseEntity.ok(response);
    }
}
