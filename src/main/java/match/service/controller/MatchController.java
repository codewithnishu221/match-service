package match.service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import match.service.dto.MatchScoreRequest;
import match.service.dto.MatchScoreResponse;
import match.service.dto.ResumeGenerationRequest;
import match.service.dto.ResumeGenerationResponse;
import match.service.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class MatchController {

    private final MatchService matchService;
    @PostMapping("/score")
    public ResponseEntity<MatchScoreResponse> createScore(@RequestBody @Valid MatchScoreRequest matchScoreRequest, HttpServletRequest httpServletRequest){
        String authHeader = httpServletRequest.getHeader("Authorization");
        MatchScoreResponse response = matchService.calculateMatchScore(matchScoreRequest, authHeader);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-resume")
    public  ResponseEntity<ResumeGenerationResponse> generateResume(@RequestBody @Valid ResumeGenerationRequest request, HttpServletRequest httpServletRequest){
         String authToken = httpServletRequest.getHeader("Authorization");
         ResumeGenerationResponse  resumeGenerationResponse = matchService.generateTailoredResume(request, authToken);
         return  ResponseEntity.ok(resumeGenerationResponse);

    }
}
